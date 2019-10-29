package ru.jdroid.devicecontrol

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.net.Socket
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // читаем activity_main.xml и отображаем его как экран
        setContentView(R.layout.activity_main)

        val display: TextView = findViewById(R.id.tv_display)
        // находим поле ввода по его id
        val editText: EditText = findViewById(R.id.et_input)
        // находим кнопку по её id
        val sentButton: Button = findViewById(R.id.btn_sent)

        // устанавливаем на кнопку обработчик нажатия
        sentButton.setOnClickListener {
            /*
            тут выполнится код в потоке UI (user interface)
            например ->
            * */
            // меняем цвет кнопки на серый
            sentButton.setBackgroundColor(Color.GRAY)
            // делаем её неактивной
            sentButton.isEnabled = false

            // стартует параллельный поток "для долгих задач"
            CoroutineScope(IO).launch {
                // тут выполняется код в потоке IO
                // (input output)

                // печатаем в окно Logcat с тагом MyDEBUG отладочную информацию
                Log.println(Log.DEBUG, "MyDEBUG", "Я IO поток.")

                // устанавливется соединение с сервером djxmm.net через 17 порт
                val connect = Socket("djxmmx.net", 17)
                // запрашиваем потоки чтения и записи (IO)
                val reader = Scanner(connect.getInputStream())
                val writer = PrintWriter(connect.getOutputStream())

                // печатаем на сервер текст из поля ввода
                writer.print(editText.text.toString())

                // создаём переменную для записи ответа в виде строки ""
                var result = ""
                // читаем каждую строчку
                while (reader.hasNextLine())
                // и прибавляем её к нашему result
                    result += reader.nextLine()

                // печатаем в окно Logcat отладочную информацию
                Log.println(Log.DEBUG, "MyDEBUG", "Server reply: $result")

                // закрываем потоки чтения и записи и само соединение
                reader.close()
                writer.flush() // flush очищает буфер неотправленных данных
                writer.close()
                connect.close()

                // симулируем задержку запроса
                delay(1000)

                // создаём ещё один UI поток
                CoroutineScope(Main).launch {

                    // печатаем в окно Logcat отладочную информацию
                    Log.println(Log.DEBUG, "MyDEBUG", "Я UI поток.")

                    // меняем цвет кнопки на цвет из файла color.xml
                    sentButton.setBackgroundColor(
                        ContextCompat.getColor(baseContext, R.color.colorPrimary)
                    )
                    // делаем её активной
                    sentButton.isEnabled = true

                    // очищаем поле ввода
                    editText.setText("")

                    // отображаем текст из запроса на экране
                    display.text = result
                }

                // печатаем в окно Logcat отладочную информацию
                Log.println(
                    Log.DEBUG,
                    "MyDEBUG",
                    "Я IO поток. Я только что породил новый UI пооток."
                )
            }

            // печатаем в окно Logcat отладочную информацию
            Log.println(Log.DEBUG, "MyDEBUG", "Я UI поток. Я только что породил новый IO пооток.")
        }
    }
}
