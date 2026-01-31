# syntax=docker/dockerfile:1.4

FROM python:3.9-slim

WORKDIR /app

# Install system dependencies (including curl for health checks)
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    build-essential \
    cmake \
    libopenblas-dev \
    liblapack-dev \
    libx11-dev \
    libgtk-3-dev \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first - cached until dependencies change
COPY requirements.txt .

# Install Python dependencies with cache mount - persists across builds!
RUN --mount=type=cache,target=/root/.cache/pip \
    pip install --upgrade pip && \
    pip install -r requirements.txt

# Copy application code - changes most frequently
COPY . .

# Expose port
EXPOSE 5000

# Run the application with optimized settings
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "5000", "--workers", "1"]
