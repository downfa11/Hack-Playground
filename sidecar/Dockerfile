FROM golang:1.25-alpine AS builder

RUN apk add --no-cache build-base gcc libpcap-dev
WORKDIR /app

COPY go.mod go.sum ./
RUN go mod tidy
RUN go mod download

COPY . .

RUN CGO_ENABLED=1 GOOS=linux go build -v -o attache .


FROM alpine:3.20

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN apk add --no-cache libpcap libcap
COPY --from=builder /app/attache .

RUN setcap cap_net_raw,cap_net_admin+eip /attache
USER appuser
ENTRYPOINT ["/attache"]