# Büyük Veri Analitiğinde Olasılıksal Veri Yapıları: HyperLogLog (HLL)

Bu proje, devasa veri akışları içerisindeki eşsiz (unique) eleman sayısını (kardinalite) bellek sınırlarına takılmadan, olasılıksal bir yaklaşımla hesaplamayı amaçlayan **HyperLogLog (HLL)** algoritmasının Java dilindeki gerçeklemesidir. Geleneksel $O(N)$ alan karmaşıklığı yaratan deterministik yöntemlerin aksine, bu algoritma sabit bir bellek alanı ile yüksek doğrulukta tahminler sunar.

##  Özellikler ve Donanım Optimizasyonları

- **Yüksek Kaliteli Hashleme:** Eşit dağılım (uniform distribution) garantisi için `MD5` algoritması kullanılarak veriler 32-bitlik tam sayılara dönüştürülmüştür.
- **Bellek Optimizasyonu (Memory Efficiency):** 32-bitlik bir hash değerinde ardışık sıfır sayısı en fazla 32 olabileceği için, kova (register) dizisi `int[]` yerine `byte[]` olarak tasarlanmıştır. Bu sayede RAM tüketimi **4 kat** azaltılmıştır.
- **Bitwise (Bit Düzeyi) Operasyonlar:** Veri ekleme (`add`) işlemlerinde string dönüşümleri yerine donanım seviyesinde çalışan bit kaydırma (`>>>`, `<<`) ve `Integer.numberOfLeadingZeros()` metotları kullanılarak maksimum hız elde edilmiştir.
- **Düzeltme Faktörleri:** Harmonik Ortalama'ya ek olarak, küçük veri setleri için "Linear Counting" (Doğrusal Sayma) ve büyük veri setleri için çakışma düzeltmeleri (Large Range Correction) entegre edilmiştir.
- **Dağıtık Sistem Uyumluluğu (Mergeable):** İki farklı HLL yapısının (`merge` metodu ile) veri kaybı olmadan birleştirilebilmesi sağlanmıştır.

##  Karmaşıklık Analizi (Complexity)

- **Zaman Karmaşıklığı (Time Complexity):** - Veri Ekleme (`add`): $O(1)$ (Sabit zaman, veri boyutundan bağımsız)
  - Tahmin ve Birleştirme (`estimate` / `merge`): $O(m)$ (Sadece kova sayısı kadar döngü)
- **Alan Karmaşıklığı (Space Complexity):** $O(m)$
  - $p=10$ bitlik bir yapı ($m = 1024$ kova) için bellek tüketimi yaklaşık **1 Kilobayt (1 KB)** seviyesindedir.

##  Standart Hata Sınırı (Standard Error)

Algoritmanın teorik tahmin sapması, kova sayısına ($m$) bağlıdır ve aşağıdaki formülle hesaplanır:

$$SE \approx \frac{1.04}{\sqrt{m}}$$

Bu projede varsayılan olarak $m = 1024$ kullanılmış olup, teorik hata payı maksimum **%3.25** civarındadır.

##  Kurulum ve Kullanım

### Gereksinimler
- Java Development Kit (JDK) 8 veya üzeri.

### Derleme ve Çalıştırma
Projeyi terminal veya komut satırı üzerinden derleyip çalıştırmak için şu adımları izleyebilirsiniz:

1. Proje dizinine gidin.
2. Java dosyasını derleyin:
   ```bash
   javac HyperLogLog.java
