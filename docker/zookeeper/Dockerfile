FROM confluentinc/cp-zookeeper:7.5.0

# Set environment variables
ENV ZOOKEEPER_CLIENT_PORT=2181
ENV ZOOKEEPER_TICK_TIME=2000
ENV ZOOKEEPER_INIT_LIMIT=5
ENV ZOOKEEPER_SYNC_LIMIT=2

# Create data directory
RUN mkdir -p /var/lib/zookeeper/data
RUN mkdir -p /var/lib/zookeeper/log

# Set permissions
RUN chown -R appuser:appuser /var/lib/zookeeper

# Expose port
EXPOSE 2181

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD echo srvr | nc localhost 2181 || exit 1

# Start Zookeeper
CMD ["/etc/confluent/docker/run"]