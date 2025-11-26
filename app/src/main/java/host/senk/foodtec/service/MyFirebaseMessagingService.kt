package host.senk.foodtec.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import host.senk.foodtec.ui.HomeActivity
import java.util.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        // ID Fijo para que coincida siempre
        const val CHANNEL_ID = "CanalFoodTec_V1"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "¡MENSAJE ENTRANDO! Origen: ${remoteMessage.from}")

        // 1. Sacar datos del mensaje
        var titulo = "Alerta FoodTec"
        var cuerpo = "Tienes una nueva notificación"

        // Si viene como "Notification" (consola o PHP básico)
        remoteMessage.notification?.let {
            titulo = it.title ?: titulo
            cuerpo = it.body ?: cuerpo
        }

        // Si viene como "Data" (PHP avanzado)
        if (remoteMessage.data.isNotEmpty()) {
            titulo = remoteMessage.data["titulo"] ?: titulo
            cuerpo = remoteMessage.data["body"] ?: cuerpo
        }

        Log.d("FCM", "Procesando: $titulo - $cuerpo")

        //  ¡FORZAR LA NOTIFICACIÓN!
        mostrarNotificacion(titulo, cuerpo)
    }

    private fun mostrarNotificacion(titulo: String, cuerpo: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        // Generar ID único para que no se reemplacen
        val notificationId = Random().nextInt(10000)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // CREAR CANAL (Obligatorio Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alertas FoodTec",
                NotificationManager.IMPORTANCE_HIGH // ¡IMPORTANCIA MÁXIMA!
            ).apply {
                description = "Notificaciones de objetos perdidos y pedidos"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true) // ¡QUE VIBRE!
                vibrationPattern = longArrayOf(0, 500, 200, 500) // Patrón: Quieto, VIBRA, quieto, VIBRA
            }
            notificationManager.createNotificationChannel(channel)
        }

        //  CONSTRUIR LA ALERTA
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Ícono de sistema seguro
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX) // ¡PRIORIDAD MÁXIMA!
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sonido + Vibración default
            .setContentIntent(pendingIntent)

        // DISPARAR!
        notificationManager.notify(notificationId, builder.build())
        Log.d("FCM", "Notificación disparada al sistema ID: $notificationId")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        // Aquí podrías actualizar la BD si cambia el token
    }
}