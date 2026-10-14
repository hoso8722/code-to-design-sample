# gcc の公式イメージには gdb が入っていないので、足す
FROM gcc:13
RUN apt-get update && apt-get install -y --no-install-recommends gdb && rm -rf /var/lib/apt/lists/*
