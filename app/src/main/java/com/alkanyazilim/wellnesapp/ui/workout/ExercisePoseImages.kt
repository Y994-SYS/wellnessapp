package com.alkanyazilim.wellnesapp.ui.workout

/**
 * Pozlar için özel PNG/çizim görselleri kullanılmıyor (karakterler tutarsız
 * çıktığı ve/veya ücretli kaynaklar gerektirdiği için vazgeçildi — bkz. proje
 * context notları). Bu fonksiyon artık her zaman null döner, bu sayede
 * CustomExerciseScreen.kt içindeki:
 *
 *   if (drawableId != null) Image(...) else StickFigure(pose, color)
 *
 * mantığı otomatik olarak StickFigure (Canvas ile çizilen basit çizgi figür)
 * yedeğine düşer. Böylece artık var olmayan drawable kaynaklarına referans
 * kalmaz ve derleme hatası ortadan kalkar.
 *
 * İleride tekrar özel görsel/animasyon eklemek istersen, ilgili pose için
 * burada R.drawable.xxx döndürecek şekilde genişletebilirsin.
 */
fun drawableForPose(pose: ExercisePose): Int? = null