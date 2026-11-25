package com.contoh.toko;

import com.contoh.toko.domain.Pelanggan;
import com.contoh.toko.domain.query.QPesanan;
import io.ebean.DB;
import java.util.List;

// TIDAK DIJALANKAN. Nama field query bean (pelanggan.nama, total.gt, ...) dihasilkan annotation processor;
// rujuk dokumentasi Ebean untuk versi yang Anda pakai.
public class DemoEbean6 {
    public static void main(String[] args) {
        // (b) pelanggan yang SEMUA ATAU SEBAGIAN pesanannya > 100.000 (query bean pada QPesanan)
        List<com.contoh.toko.domain.Pesanan> besar = new QPesanan().total.gt(100_000).fetch("pelanggan").findList();
        besar.forEach(o -> System.out.println(o.getPelanggan().getNama() + ": pesanan " + o.getTotal()));

        // (b) pelanggan yang TOTAL pesanannya > 100.000: agregasi paling jelas lewat SQL native + group by
        DB.sqlQuery("select p.nama, sum(o.total) as total from pelanggan p join pesanan o on o.pelanggan_id = p.id "
                        + "group by p.nama having sum(o.total) > 100000 order by p.nama")
                .findList().forEach(r -> System.out.println(r.getString("nama") + ": " + r.getLong("total")));

        // (c) bandingkan SQL di log: tanpa vs dengan fetch("pesanan")
        for (Pelanggan p : DB.find(Pelanggan.class).findList()) p.getPesanan().size();                 // lazy: query tambahan
        for (Pelanggan p : DB.find(Pelanggan.class).fetch("pesanan").findList()) p.getPesanan().size();   // satu query join
    }
}
