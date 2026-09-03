WellnesApp — Proje Context Dosyası (Güncel)
Genel Bilgiler
Paket adı: com.alkanyazilim.wellnesapp
Dil/UI: Kotlin + Jetpack Compose
Mimari: MVVM + Repository katmanı
Min SDK: 26 | Compile SDK: 37 | Target SDK: 36
Kotlin: 2.2.10 | KSP: 2.2.10-2.0.2 | AGP: 9.1.1
Versiyon: 1.0 (versionCode 1)
Play Console durumu: Dahili test aşamasında, yayınlanmış durumda (%100 sunum)
Navigasyon Yapısı
Alt navigasyon (bottom bar) — 5 sekme, AppNavigation.kt (Navigation Compose, NavHost + Screen sealed class):
Ana Sayfa — özet kartlar, üstte Ayarlar ikonu
Adım — Health Connect entegrasyonu
Su — su takibi + hatırlatıcı + istatistikler (haftalık/aylık/yıllık)
Görevler — Room tabanlı görev/alışkanlık takibi
Egzersiz (WorkoutHubScreen) — sekme içi kategoriler: Koşu / Kardiyo / Güç / Esneme
Ayrıca: Ayarlar ekranı (route: settings, Ana Sayfa'dan ikonla açılıyor)
Veri Katmanı
DataStore (Preferences)
UserPreferences — günlük adım hedefi
WaterDataStore — su hedefi, bardak boyutu, günlük tüketim, hatırlatıcı ayarları (sıklık, saat aralığı, ses açık/kapalı, seçilen zil sesi URI'si), tüm günlük kayıtları okuyan allConsumedEntries Flow'u (istatistik ve yedekleme için)
AppSettingsDataStore — tema modu, kullanıcı profili (isim, kilo, boy, yaş)
Room Database (AppDatabase, versiyon 5)
TaskEntity / TaskCompletionEntity / TaskDao
RunSessionEntity / RunSessionDao — koşu geçmişi (başlangıç/bitiş, adım, hedef, süre, goalType ve targetDurationSeconds — v5'te eklendi)
Migration sistemi artık disiplinli: exportSchema = true (şema JSON'ları app/schemas/'a kaydediliyor, Git'e eklenmeli), fallbackToDestructiveMigrationFrom(1, 2, 3) sadece kullanıcısız eski dev versiyonları kapsıyor. Versiyon 4 ve sonrası için gerçek Migration nesneleri zorunlu — MIGRATION_4_5 bunun ilk örneği (RunSessionEntity'ye yeni alan ekledi, veri kaybı olmadan). Migration eksikse Room artık sessizce veri silmek yerine çöküyor (kasıtlı güvenlik ağı).
Yedekleme (Export/Import) — YENİ
BackupManager.kt — tüm veriyi (profil, hedefler, görevler, tamamlamalar, koşu geçmişi, su kayıtları) tek bir JSON dosyasına aktarır/geri yükler
Storage Access Framework (ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT) kullanılıyor, ekstra kütüphane yok (org.json)
Görev/koşu ID'leri geri yüklerken korunuyor (foreign-key referansları bozulmasın diye)
Ayarlar ekranında "Yedekle ve Geri Yükle" bölümü, içe aktarma öncesi onay diyaloğu var
Health Connect (HealthConnectManager)
Bugünün adımı, belirli tarih için adım, son N günün adımları
Ekranlar ve Özellikler
Adım
İzin akışı, bugünün adımı, geçmiş 14 gün, detay ekranı
Türk kültürüne özgü karşılaştırmalar eklendi: lahmacun, pide, Adana kebap, baklava, simit, künefe (kalori); Çamlıca Kulesi, Galata Kulesi, 15 Temmuz Şehitler Köprüsü (mesafe) — yabancı örneklerle karışık, toplam 14 kalori + 9 mesafe karşılaştırması
RunTrackingService (Foreground Service) — süre bazlı wall-clock hesaplama ile düzeltildi (Doze modu kaynaklı "birkaç dakika eksik" hatası çözüldü)
RunHistoryScreen — yenilenmiş kart tasarımı (saat ikonu, süre rozeti, adım/mesafe ikonları), hedef türüne göre farklı gösterim (süre hedefliyse "X dk hedef", adım hedefliyse "X / Y adım")
Koşu (RunScreen.kt) — GENİŞLETİLDİ
Artık iki hedef modu var: Adım VEYA Süre
Süre modunda: 15/30/45/60 dk hızlı seçim + ±5 dk özel ayar
Süre dolunca ayrı, sesli + titreşimli bir bildirim (ana takip bildiriminden farklı kanal, IMPORTANCE_HIGH)
Aktif ekranda hedefe göre halka rengi yeşile dönüyor, "🎉 Hedefine ulaştın!" mesajı
RunSessionEntity'ye goalType ve targetDurationSeconds eklendi (v5 migration)
Su
Dairesel gösterge, bardak ızgarası, +/- ile miktar
Zil sesi seçimi eklendi — RingtoneManager ile sistem ses seçici, seçilen ses WaterDataStore'a kaydediliyor
Bildirim kanalı bug'ı düzeltildi: artık her farklı ses için dinamik kanal ID'si üretiliyor (water_reminder_channel_sound_<hash>) — eski "sil + yeniden oluştur" yaklaşımı bazı cihazlarda ses değişikliğini yansıtmıyordu
AlarmScheduler gece yarısı bug'ı düzeltildi: saat karşılaştırması yerine gerçek zaman karşılaştırması (!next.after(now)) kullanılıyor — eskiden gece yarısını aşan alarmlar sonsuz döngüye giriyordu
İstatistikler kartı eklendi: Haftalık (Pazartesi-Pazar takvim haftası, her Pazartesi sıfırlanır), Aylık, Yıllık toplam + "günlük ortalama" (dönemin tam uzunluğuna değil, o ana kadar geçen gün sayısına bölünüyor — daha gerçekçi)
Görevler
Kategoriye göre gruplu liste, checkbox ile tamamlama, hazır şablonlar + özel ikon seçici
Egzersiz (WorkoutHubScreen + CustomExerciseScreen)
Koşu, Kardiyo, Güç, Esneme kategorileri arası sekme geçişi
Egzersiz kütüphanesi: 52 hazır şablon (Kardiyo 20, Güç 23, Esneme 9)
Sesli koçluk sistemi eklendi (VoiceCue, cycleSeconds):
Her egzersiz için, bir "tekrar döngüsü" içinde yüzdeye göre tetiklenen sesli komutlar (örn. squat: %0 "Kalçanı geri it", %45 "Aşağı in, tut", %75 "Kalk")
Süreli modda: döngü toplam süre boyunca tekrar eder (hızlı hareketlerde kısa döngü, duruşlarda uzun hatırlatma döngüsü)
Set bazlı modda: döngü, tekrar sayısı kadar çalışır, her tekrar başında sayıyı da söyler ("3. tekrar: Aşağı in")
52 şablonun tamamına voiceCues verisi tanımlandı
Mute butonu eklendi — aktif oturumda sesi anında açıp kapatabiliyor
TTS artık düzgün susuyor: hem onDispose'da hem de ON_PAUSE/ON_STOP lifecycle olaylarında (LocalLifecycleOwner üzerinden, NavBackStackEntry'ye özel) zorla durduruluyor — sekme değişince/ekrandan çıkınca konuşmaya devam etme sorunu çözüldü
StickFigure görselleştirmesi yükseltildi: eski ince tek-renkli çizgi yerine, eklem noktalı (beyaz halka + renkli merkez), gradyanlı gövde/baş, zemin gölgeli — pose-tracking (MediaPipe tarzı) görünüm
ExercisePoseImages.kt — kullanıcının PNG denemesi tutarsız çıktığı için (drawableForPose() her zaman null dönüyor, StickFigure'a düşüyor). 52 egzersiz için tutarlı karakterli (hafif sakallı, diz altı kapri) PNG üretim promptları ayrı bir dosyada hazırlandı (egzersiz-png-promptlari.md), kullanıcı zamanla üretip ekleyecek
Ayarlar
Tema, profil, hedefler
YENİ: Yedekle ve Geri Yükle bölümü (yukarıda detaylı)
Tema / Renk Sistemi — GÜNCELLENDİ
Ana marka rengi mor-indigo'ya çekildi: Purple40 = #7C5CFC (eskiden #7E57C2)
Pink40 (tertiary) sert "hot pink"ten (#E91E8C) yumuşak mercan-kırmızıya (#E85D75) çekildi
Koyu tema arka planı #1C1B22 → #15141C (mor marka rengiyle daha uyumlu)
Ekran bazlı vurgu renkleri artık aynı doygunluk/parlaklık ailesinde:
Su: #3B9EE8 (mavi) · Adım: #FF7A59 (mercan/turuncu) · Görevler: #7C5CFC (marka rengi) · Egzersiz: #E85D75 (mercan-kırmızı) · Ana Sayfa: #7C5CFC (nötr)
Kod Kalitesi Düzeltmeleri
"Reading locale in a non-observable way" lint uyarısı çözüldü: LocaleUtils.kt içinde rememberCurrentLocale() yardımcı fonksiyonu oluşturuldu (LocalConfiguration.current üzerinden observable okuma), tüm Locale.getDefault() kullanımları buna bağlandı (StepsDetailScreen, RunHistoryScreen, RunScreen, CustomExerciseScreen)
Play Store Yayın Hazırlığı
Gizlilik Politikası hazırlandı (privacy-policy.html), GitHub Pages üzerinde barındırılıyor
Store listing metinleri yazıldı: kısa açıklama (68/80 karakter), uzun açıklama (1787/4000 karakter)
Featured graphic (1024×500 PNG) Python/Pillow ile programatik olarak üretildi — marka renklerinde, su damlası ikonlu, 4 pillar etiketli
Sağlık uygulamaları beyan formu dolduruldu (Activity Recognition, READ_STEPS izin gerekçeleri)
Ön Plan Hizmeti (Foreground Service) beyanı dolduruldu ("Sağlık" tipi, koşu takibi gerekçesi)
Dahili test sürümü başarıyla yayınlandı, tester opt-in süreci tamamlandı
Denenip Vazgeçilen Şeyler
Çizgi figür (stick figure) illüstrasyonları — artık YENİDEN KULLANILIYOR (yükseltilmiş haliyle), önceki "vazgeçildi" notu geçersiz
Lottie animasyonları — hâlâ kullanılmıyor, temizlenebilir
Kullanıcının kendi PNG denemesi — karakterler tutarsız çıktı, tutarlı prompt seti hazırlanıp yerine StickFigure bırakıldı
Henüz Yapılmayanlar
Hafta 6 — İstatistikler & Grafikler: Su istatistikleri artık var; adım/görev/genel grafik kütüphanesi, streak takibi, rozet sistemi hâlâ yapılmadı
Hafta 8 — Test, Optimizasyon, Yayın Hazırlığı: Play Store yayın süreci büyük ölçüde ilerledi (dahili test aşamasında); unit/UI testler hâlâ yapılmadı
Kalan ~52 egzersiz PNG'si kullanıcı tarafından üretilip eklenmeyi bekliyor (opsiyonel, StickFigure zaten çalışıyor)
Bilinen Kırılgan Noktalar (Güncel)
StickFigure.kt ve Lottie ile ilgili dosyalar/bağımlılıklar hâlâ kısmen kullanılmıyor (Lottie), temizlenebilir
Health Connect'ten gelen veri, telefonun kendi adım sayar uygulamasıyla birebir eşleşmeyebilir (veri kaynağı farkı, hata değil)
app/schemas/ klasörünün Git'e eklenip eklenmediği doğrulanmalı (migration testleri için gerekli)
Play Store targetSdk gereksinimleri yıllık değişebilir — her büyük güncellemede kontrol edilmeli