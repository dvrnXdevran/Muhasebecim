# Muhasebecim

Muhasebecim, küçük işletmelerin, işverenlerin ve günlük para trafiğini düzenli tutmak isteyen kullanıcıların alacak-verecek takibini kolaylaştırmak için geliştirilmiş bir Android uygulamasıdır.

Uygulamanın temel amacı; deftere, not kağıdına, WhatsApp mesajlarına veya dağınık Excel dosyalarına yazılan borç-alacak kayıtlarını tek bir yerde toplamak ve işletme sahibinin kiminle ne hesabı olduğunu hızlıca görebilmesini sağlamaktır.

Bir işveren için en yorucu konulardan biri, gün içinde farklı kişilerle yapılan ödemeleri, alınacak paraları, verilecek borçları ve ileri tarihli tahsilatları akılda tutmaya çalışmaktır. Muhasebecim bu yükü azaltmak için kişi bazlı kayıt mantığıyla çalışır. Her kişi için ayrı bir hesap sayfası tutulur; o kişiye ait alacaklar, verecekler, ödenenler, bekleyenler ve özel notlar aynı yerde görünür.

## İşverenler İçin Faydaları

Bir işveren ya da işletme sahibi için para takibinde en önemli şey netliktir. Gün içinde birçok kişiyle işlem yapılır; biri mal alır, biri borcunu erteler, bir tedarikçiye ödeme sözü verilir, bir çalışanla hesap görülür. Bunların hepsi küçük notlar halinde kalırsa zamanla karışır.

Muhasebecim bu karmaşayı azaltır:

- Her kişi için ayrı alacak-verecek takibi yapılabilir.
- Kişinin geçmiş hareketleri tek ekranda görülebilir.
- Ödenmiş ve ödenmemiş kayıtlar ayırt edilebilir.
- İleri tarihli alacak ve verecekler unutulmadan takip edilebilir.
- Kişiye özel notlar eklenebilir.
- Genel işletme hareketleri kişi bağımsız olarak tutulabilir.
- Yaklaşan kayıtlar üzerinden hangi ödemenin veya tahsilatın sırada olduğu görülebilir.

Bu sayede işletme sahibi sürekli geçmiş mesajları aramak, defter karıştırmak veya "Ben bunu yazmış mıydım?" diye düşünmek zorunda kalmaz. Uygulama, hesap takibini daha düzenli ve daha güvenilir hale getirir.

## Temel Özellikler

### Kişi Bazlı Alacak-Verecek Takibi

Her müşteri, çalışan, tedarikçi veya iş ortağı ayrı bir kişi olarak kaydedilebilir. Kişinin detay ekranında yalnızca o kişiye ait işlemler görünür. Bu yapı, işletme sahibinin her kişiyle olan hesabını ayrı ayrı takip etmesini kolaylaştırır.

### Alacak ve Verecek Kayıtları

Para girişleri ve çıkışları alacak/verecek mantığıyla kaydedilir. Kayıtlara açıklama, tutar ve tarih bilgisi eklenebilir. Ödeme gerçekleştiğinde kayıt ödenmiş olarak işaretlenebilir.

### İleri Tarihli Kayıtlar

Vadeli ödemeler, yaklaşan tahsilatlar veya belirli bir tarihte hatırlanması gereken işler ileri tarihli kayıt olarak eklenebilir. Bu özellik, özellikle "haftaya alacağım", "ayın sonunda ödeyeceğim", "şu tarihte hatırlat" gibi durumlar için tasarlanmıştır.

### Notlar

Kişilere özel notlar tutulabilir. Örneğin anlaşma detayı, teslimat bilgisi, ödeme açıklaması veya unutulmaması gereken herhangi bir bilgi kişi ekranına eklenebilir.

### Genel Defter

Kişiye bağlı olmayan işletme hareketleri için genel defter bölümü bulunur. Bu alan, genel alacak-verecekler, işletme notları ve kişiyle ilişkilendirilmeyen kayıtlar için kullanılabilir.

### Yaklaşan İşler

İleri tarihli kayıtlar sayesinde yaklaşan alacaklar, verecekler ve notlar daha kolay takip edilir. Bu da özellikle yoğun iş temposunda unutulan ödemelerin ve tahsilatların önüne geçmeye yardımcı olur.

## Yedekleme Durumu

Uygulama ilk geliştirilirken tek kişinin kullanımı hedeflenmiştir. Bu nedenle yedekleme sistemi kişisel bir sunucu ve SQL veritabanı üzerinden çalışacak şekilde düşünülmüştür.

Şu anda yedekleme bölümü aktif olarak çalışır durumda kabul edilmemelidir. Uygulama yerel veritabanı ile cihaz üzerinde kullanılabilir; ancak sunucuya yedek alma ve sunucudan geri yükleme özelliklerinin çalışması için uygun bir SQL veritabanı ve sunucu bağlantısı yapılmalıdır.

Yedekleme altyapısı için `server/backup-api/` klasöründe PHP ve SQL tarafına ait dosyalar bulunur. Bu repoda herhangi bir gerçek sunucu adresi, veritabanı bilgisi veya token bulunmaz. Bir sunucuda veritabanı oluşturulup bağlantı bilgileri girildiğinde, Android tarafındaki yedekleme ayarları da aynı sunucuya göre düzenlenerek bu özellik tekrar kullanılabilir hale getirilebilir.

Kısaca:

- Uygulama Android cihazda yerel olarak kullanılabilir.
- Alacak-verecek ve kişi kayıtları cihazdaki yerel veritabanında tutulur.
- Yedekleme ekranı vardır, fakat aktif kullanım için SQL/sunucu bağlantısı gerekir.
- SQL bağlantısı ve API ayarları yapılmadan yedekleme özelliği çalışmaz.

## Kullanılan Teknolojiler

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Room veritabanı
- PHP + SQL yedekleme altyapısı
