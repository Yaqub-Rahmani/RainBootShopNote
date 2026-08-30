# Geliştirme öncesi yerel başlangıç kaydı

Tarih: 30 Ağustos 2026. Kullanıcı beyanı: yaklaşık 200 aktif kullanıcı.
Önceki HEAD: `2022c75e7cc83174512e8c8088ed5769f26706e7`.

Bu kayıt mevcut çalışma ağacındaki `LoginActivity.java` PIN görünürlük düzeltmesini
ve `DBHelper.java` değişikliklerini olduğu gibi sabitler. DBHelper'ın yorum satırı
halindeki eski kodu da korunmuştur. Yeni işlevsel kod değişikliği yapılmamıştır.
Keystore, özel yapılandırma ve yerel müşteri DB/yedekleri için ignore kuralları eklendi.

Bu, çalışan APK'nın kaynaktan yeniden üretildiği, testlerin geçtiği veya veri taşımanın
güvenli olduğu anlamına gelmez. Bu görevde build, indirme, kurulum, migration ve push yoktur.

## Commit dışındaki yerel dosyalar

- `app/release/myApkKeyed.jks`: özel imzalama dosyası; diskte korunur, Git'e alınmaz.
- `app/release/RainBootV2.apk`: gömülü SMTP parolası nedeniyle değiştirilmiş APK alınmaz.
- `app/release/baselineProfiles/0/app-release.dm` ve `1/app-release.dm`: APK'nın üretilmiş yardımcı çıktıları.
- `.idea/misc.xml`: IDE kaynaklı XML bildirimi farkı; kullanıcı dosyası korunur.

Yerel APK SHA-256:
`6F18775A9881BE44D446B38EF19D028E415EF6EB691928A05426BEC62A8D053D`.
APK paket/sürüm: `com.example.rainbootshopnote`, versionCode `1`, versionName `1.0`.
Bu APK'nın mevcut kaynak ağacıyla birebir eşleştiği veya tüm kullanıcılardaki en yüksek
sürümü temsil ettiği doğrulanmamıştır. Tam çalışma ağacı/binary snapshot'ı değildir.

## Güvenlik sınırı ve push engeli

`app/src/main/java/com/example/rainbootshopnote/utils/EmailSender.java:25` içindeki
gömülü SMTP parolası önceki HEAD'de ve yerel `origin/main` kaydında zaten bulunmaktadır;
aynı değer yerel APK'nın `classes.dex` içeriğinde de saptanmıştır. Değeri bu nota yazılmadı.
Dosya ve geçmiş bu görevde değiştirilmedi. Bu commit eski ağaç/geçmişteki sırrı ortadan
kaldırmaz; depo veya commit'in tüm ağacı için "sır içermiyor" iddiası yoktur.
Parolanın iptal/yenilenmesi ve kaynak/artefakt/geçmiş yayımlama kapsamı kararı gerekir.
Uzak deponun erişim durumu ve güncel remote HEAD ağ bağlantısıyla doğrulanmadı.

Önceden izlenen `app/src/main/assets/rainbootnote.db` değiştirilmedi ve yeniden stage
edilmedi. Mantıksal iş tabloları boş, `sqlite_sequence` içinde 7 sayaç kaydı vardır.
Boş tablo sayımları silinmiş veri kalıntılarının bulunmadığına dair adli inceleme değildir.
Bu eski asset'in mevcut Git geçmişinde bulunduğu ayrıca dikkate alınmalıdır.

Sır kontrolü: 190 izlenen metin dosyasında yaygın özel anahtar, service-account,
token ve sabit parola örüntüleri; ilgili APK'da bilinen parolanın varlık kontrolü;
asset DB'de salt okunur kayıt sayımları. `google-services.json` Android istemci
yapılandırmasıdır; service-account özel anahtarı olarak sınıflandırılmadı.
Bu tarama bütün sır türlerinin veya geçmişteki tüm binary içeriklerin yokluğunu kanıtlamaz.

## Başlangıç kaydından sonraki dar güvenlik temizliği

Başlangıç commit'i `135a64467b6b43f15682454fc6ee505e3fde9093` aynen korunmuştur.
Sonraki ayrı temizlikte, etkin çağrı/import'u bulunmayan `EmailSender.java` ile yalnız
ona ait `javax.mail` ve `activation` bağımlılıkları kaldırılmıştır. Yerel yedek ve
Firebase yedeği başka sınıflarda çalışır; bu akışların kodu değiştirilmemiştir.
Bu, yukarıdaki tarihsel başlangıç kaydının kapsamını geriye dönük değiştirmez.

Canlı uzak dal okumasında `origin/main`,
`2022c75e7cc83174512e8c8088ed5769f26706e7` olarak doğrulanmıştır; gömülü SMTP sırrının
aynı kaynak blobu bu uzak commit'te zaten bulunmaktadır. Güncel kaynak temizliği eski
Git geçmişini, yereldeki/dağıtılmış APK'ları veya kimlik bilgisinin geçerliliğini
değiştirmez. Hesaba erişilmemiş, parola denenmemiş/iptal edilmemiş, geçmiş yeniden
yazılmamıştır. Derleme, APK yayını veya veri koruyan güncelleme testi iddiası yoktur.
