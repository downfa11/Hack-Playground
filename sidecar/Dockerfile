FROM golang:1.21-alpine as builder
WORKDIR /app
COPY main.go .
RUN go build -o proxy .

FROM alpine
WORKDIR /app
COPY --from=builder /app/proxy .
ENTRYPOINT ["./proxy"]