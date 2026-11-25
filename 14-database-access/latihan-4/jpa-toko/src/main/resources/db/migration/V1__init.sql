CREATE TABLE pelanggan (
    id      BIGSERIAL PRIMARY KEY,
    nama    VARCHAR(100) NOT NULL,
    kota    VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0          -- kolom untuk @Version
);
CREATE TABLE pesanan (
    id           BIGSERIAL PRIMARY KEY,
    pelanggan_id BIGINT NOT NULL REFERENCES pelanggan(id),
    total        BIGINT NOT NULL,
    dibuat       TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_pesanan_pelanggan ON pesanan(pelanggan_id);
