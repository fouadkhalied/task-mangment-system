FROM redis:7-alpine

# Create data directory
RUN mkdir -p /data

# Set working directory
WORKDIR /data

# Start Redis with basic configuration
CMD ["redis-server", "--bind", "0.0.0.0", "--protected-mode", "no", "--appendonly", "yes", "--dir", "/data"]

# Expose Redis port
EXPOSE 6379