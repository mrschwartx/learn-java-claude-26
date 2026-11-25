-- Skema dari materi Modul 14 (+ tabel riwayat_transfer untuk Latihan 2). Aman dijalankan ulang.
DROP TABLE IF EXISTS riwayat_transfer, pesanan, pelanggan, rekening CASCADE;

CREATE TABLE rekening (
    id      BIGSERIAL PRIMARY KEY,
    pemilik VARCHAR(100) NOT NULL,
    saldo   BIGINT NOT NULL CHECK (saldo >= 0)
);

CREATE TABLE pelanggan (
    id    BIGSERIAL PRIMARY KEY,
    nama  VARCHAR(100) NOT NULL,
    kota  VARCHAR(50)
);

CREATE TABLE pesanan (
    id           BIGSERIAL PRIMARY KEY,
    pelanggan_id BIGINT NOT NULL REFERENCES pelanggan(id),
    total        BIGINT NOT NULL,
    dibuat       TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_pesanan_pelanggan ON pesanan(pelanggan_id);

CREATE TABLE riwayat_transfer (
    id        BIGSERIAL PRIMARY KEY,
    dari      BIGINT NOT NULL REFERENCES rekening(id),
    ke        BIGINT NOT NULL REFERENCES rekening(id),
    jumlah    BIGINT NOT NULL CHECK (jumlah > 0),
    dicatat   TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO rekening (pemilik, saldo) VALUES ('Andi', 500000), ('Budi', 100000);
INSERT INTO pelanggan (nama, kota) VALUES ('Ani', 'Bandung'), ('Candra', 'Jakarta'), ('Dewi', 'Bandung');
INSERT INTO pesanan (pelanggan_id, total) VALUES (1, 50000), (1, 75000), (2, 20000), (3, 120000);
