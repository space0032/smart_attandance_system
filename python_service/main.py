from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import RedirectResponse
import face_recognition
import numpy as np
import cv2
import uvicorn
from typing import List
from pydantic import BaseModel

app = FastAPI(
    title="Smart Attendance Face Recognition API",
    description="Face recognition service for smart attendance system",
    version="1.0.0"
)

# Store known face encodings and student IDs
known_face_encodings = []
known_face_ids = []

@app.get("/")
async def root():
    """Root endpoint - redirects to API documentation"""
    return RedirectResponse(url="/docs")

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "service": "Face Recognition API",
        "registered_faces": len(known_face_encodings),
        "version": "1.0.0"
    }

class RegisterRequest(BaseModel):
    student_id: str
    encoding: List[float]

@app.post("/register")
async def register_face(request: RegisterRequest):
    try:
        known_face_encodings.append(np.array(request.encoding))
        known_face_ids.append(request.student_id)
        return {"message": "Face registered successfully"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/recognize")
async def recognize_face(file: UploadFile = File(...)):
    try:
        # Read image file
        contents = await file.read()
        nparr = np.frombuffer(contents, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        
        if img is None:
            raise HTTPException(status_code=400, detail="Invalid image format")
        
        # Convert to RGB (face_recognition uses RGB)
        rgb_img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        
        # Optimization: Resize image to 1/4 size for faster face recognition processing
        # This significantly speeds up detection with minimal accuracy loss for reasonably sized faces
        small_img = cv2.resize(rgb_img, (0, 0), fx=0.25, fy=0.25)
        
        # Detect faces in the resized image
        face_locations = face_recognition.face_locations(small_img)
        face_encodings = face_recognition.face_encodings(small_img, face_locations)
        
        results = []
        
        for encoding in face_encodings:
            # Check if we have any known faces
            if not known_face_encodings:
                results.append({"student_id": "UNKNOWN", "confidence": 0.0})
                continue
                
            # Compare with known faces
            matches = face_recognition.compare_faces(known_face_encodings, encoding, tolerance=0.6)
            face_distances = face_recognition.face_distance(known_face_encodings, encoding)
            
            best_match_index = np.argmin(face_distances)
            
            if matches[best_match_index]:
                student_id = known_face_ids[best_match_index]
                confidence = 1.0 - face_distances[best_match_index]
                results.append({"student_id": student_id, "confidence": confidence})
            else:
                results.append({"student_id": "UNKNOWN", "confidence": 0.0})
                
        return {"matches": results}
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.post("/encode")
async def encode_face(file: UploadFile = File(...)):
    try:
        contents = await file.read()
        nparr = np.frombuffer(contents, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        
        if img is None:
            raise HTTPException(status_code=400, detail="Invalid image format")
            
        rgb_img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        
        encodings = face_recognition.face_encodings(rgb_img)
        
        if len(encodings) > 0:
            return {"encoding": encodings[0].tolist(), "found": True}
        else:
            return {"found": False}
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=5000)
