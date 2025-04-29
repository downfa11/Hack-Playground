FROM golang:1.24 as builder

WORKDIR /app

RUN apt-get update && apt-get install -y libpcap-dev

COPY go.mod go.sum ./
RUN go mod tidy
RUN go mod download

COPY . .

RUN CGO_ENABLED=1 GOOS=linux go build -v -o attache attache.go

FROM alpine:latest

WORKDIR /

COPY --from=builder /app/attache .

RUN apk update && apk add --no-cache libpcap libc6-compat gcompat libpcap-dev
RUN ln -s /usr/lib/libpcap.so /usr/lib/libpcap.so.0.8
ENV LD_LIBRARY_PATH=/usr/lib:/lib:/usr/local/lib

RUN chmod +x /attache

EXPOSE 8080

ENTRYPOINT ["/attache"]