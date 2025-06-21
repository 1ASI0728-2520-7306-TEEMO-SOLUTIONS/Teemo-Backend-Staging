FROM ubuntu:latest
LABEL authors="fabri"

ENTRYPOINT ["top", "-b"]