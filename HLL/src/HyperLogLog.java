import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HyperLogLog {
    private final int p;
    private final int m;
    private final byte[] registers;
    private final double alpha;

    /**
     * HLL Sınıfı Başlatıcı
     * @param p Kova (bucket) indeksini belirlemek için kullanılacak bit sayısı.
     */
    public HyperLogLog(int p) {
        this.p = p;
        this.m = 1 << p; // m = 2^p
        this.registers = new byte[this.m];


        if (this.m == 16) {
            this.alpha = 0.673;
        } else if (this.m == 32) {
            this.alpha = 0.697;
        } else if (this.m == 64) {
            this.alpha = 0.709;
        } else {
            this.alpha = 0.7213 / (1 + 1.079 / this.m);
        }
    }

    /** 1. Yüksek Kaliteli Hash Fonksiyonu */
    private int hash(String item) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(item.getBytes());
            return ((digest[0] & 0xFF) << 24) |
                    ((digest[1] & 0xFF) << 16) |
                    ((digest[2] & 0xFF) << 8)  |
                    (digest[3] & 0xFF);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algoritması bulunamadı", e);
        }
    }

    /** Veriyi HLL yapısına ekler. */
    public void add(String item) {
        int x = hash(item);

        // 2. Kovalama (Bucketing) Mekanizması
        // İlk 'p' biti almak için sağa mantıksal kaydırma (Logical right shift)
        int idx = x >>> (32 - p);

        // 3. Ardışık Sıfır Sayısını Bulma
        // Kova bitlerini yok etmek için sayıyı sola kaydırıyoruz
        int w = x << p;


        int rho = Integer.numberOfLeadingZeros(w) + 1;


        if (rho > this.registers[idx]) {
            this.registers[idx] = (byte) rho;
        }
    }

    /** Kardinalite tahminini hesaplar. */
    public int estimate() {
        double Z = 0.0;
        int V = 0; // Boş kova sayısı


        for (byte val : registers) {
            Z += Math.pow(2.0, -val);
            if (val == 0) {
                V++;
            }
        }

        double E = alpha * (m * m) / Z;

        // 5. Düzeltme Faktörleri
        if (E <= 2.5 * m) {

            if (V > 0) {
                E = m * Math.log((double) m / V);
            }
        } else if (E > (1.0 / 30.0) * (1L << 32)) {
            E = - (1L << 32) * Math.log(1 - E / (1L << 32));
        }

        return (int) E;
    }

    /** 6. Birleştirilebilir Özellik (Merge) */
    public HyperLogLog merge(HyperLogLog other) {
        if (this.p != other.p) {
            throw new IllegalArgumentException("Birleştirme işlemi için 'p' (kova) değerleri aynı olmalıdır.");
        }

        HyperLogLog merged = new HyperLogLog(this.p);


        for (int i = 0; i < this.m; i++) {
            merged.registers[i] = (byte) Math.max(this.registers[i], other.registers[i]);
        }
        return merged;
    }

    // --- ÖDEV TEST VE KONSOL ÇIKTISI BÖLÜMÜ ---
    public static void main(String[] args) {
        System.out.println("--- HyperLogLog (HLL) Algoritması Testi ---\n");

        int p_degeri = 10;
        HyperLogLog hll_A = new HyperLogLog(p_degeri);
        HyperLogLog hll_B = new HyperLogLog(p_degeri);

        int gercek_sayi_A = 20000;
        int gercek_sayi_B = 15000;

        System.out.println("Veriler ekleniyor... (Lütfen bekleyin)");

        for (int i = 0; i < gercek_sayi_A; i++) {
            hll_A.add("eleman_A_" + i);
        }
        for (int i = 0; i < gercek_sayi_B; i++) {
            hll_B.add("eleman_B_" + i);
        }


        int ortak_eleman_sayisi = 5000;
        for (int i = 0; i < ortak_eleman_sayisi; i++) {
            String ortak_eleman = "ortak_eleman_" + i;
            hll_A.add(ortak_eleman);
            hll_B.add(ortak_eleman);
        }

        int tahmin_A = hll_A.estimate();
        int tahmin_B = hll_B.estimate();

        System.out.println("\n--- Bireysel HLL Tahminleri ---");
        System.out.printf("HLL A - Gerçek: 25000 | Tahmin: %d | Hata Payı: %%%.2f\n", tahmin_A, Math.abs(25000.0 - tahmin_A) / 25000.0 * 100);
        System.out.printf("HLL B - Gerçek: 20000 | Tahmin: %d | Hata Payı: %%%.2f\n", tahmin_B, Math.abs(20000.0 - tahmin_B) / 20000.0 * 100);

        HyperLogLog hll_birlesim = hll_A.merge(hll_B);
        int tahmin_birlesim = hll_birlesim.estimate();

        System.out.println("\n--- Birleştirilmiş (Merged) HLL Tahmini ---");
        System.out.println("Beklenen Toplam Eşsiz Eleman (A U B): 40000");
        System.out.println("Birleşmiş HLL Tahmini: " + tahmin_birlesim);
        System.out.printf("Birleşim Hata Payı: %%%.2f\n", Math.abs(40000.0 - tahmin_birlesim) / 40000.0 * 100);

        int m = 1 << p_degeri;
        double teorik_hata = (1.04 / Math.sqrt(m)) * 100;
        System.out.printf("\nTeorik Olarak Beklenen Maksimum Standart Hata (m=%d): %%%.2f\n", m, teorik_hata);
    }
}