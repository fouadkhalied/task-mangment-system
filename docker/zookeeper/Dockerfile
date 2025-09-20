FROM confluentinc/cp-zookeeper:7.5.0

# Environment variables
ENV ZOOKEEPER_CLIENT_PORT=2181
ENV ZOOKEEPER_CLIENT_PORT_ADDRESS=0.0.0.0
ENV ZOOKEEPER_TICK_TIME=2000
ENV ZOOKEEPER_INIT_LIMIT=5
ENV ZOOKEEPER_SYNC_LIMIT=2
ENV ZOOKEEPER_SERVER_ID=1
ENV ZOOKEEPER_SERVERS=server.1=0.0.0.0:2888:3888

# Create data + log dirs
RUN mkdir -p /var/lib/zookeeper/data /var/lib/zookeeper/log && \
    chown -R appuser:appuser /var/lib/zookeeper

EXPOSE 2181 2888 3888

# Healthcheck for Zookeeper
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD echo srvr | nc 127.0.0.1 2181 || exit 1

CMD ["/etc/confluent/docker/run"]
