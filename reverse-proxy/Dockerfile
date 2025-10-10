FROM golang:1.25-alpine AS builder

RUN apk add --no-cache git
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -ldflags="-s -w" -o detache .


FROM alpine:3.20

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /app/detache /usr/local/bin/detache
COPY /wsclient.html /wsclient.html
USER appuser

CMD ["/usr/local/bin/detache"]