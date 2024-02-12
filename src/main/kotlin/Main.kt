package org.example

import java.io.*
import java.nio.charset.Charset
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis


class WeatherData (var sum: Double, var min: Double, var max: Double, var count:Int){
}
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val executionTime = measureTimeMillis {
        val answer: HashMap<String, WeatherData> = HashMap()
        val output: ArrayList<HashMap<String, WeatherData>> = ArrayList<HashMap<String, WeatherData>>()

        val fileCount = splitFile(File("measurements.txt"), 500)
        println("completed")
        val threads = (1..fileCount).map { it ->
            thread {
                output.add(handleFile(File("measurements/measurements$it.txt")))
            }
        }
        threads.forEach { it.join() }

        for (data in output){
            for (weather in data.keys){
                if (answer.contains(weather)){
                    answer[weather]?.sum = data[weather]?.let { answer[weather]?.sum?.plus(it.sum) }!!
                    answer[weather]?.count = data[weather]?.let { answer[weather]?.count?.plus(it.count) }!!
                    if (answer[weather]?.min!! > data[weather]?.min!!){
                        answer[weather]?.min = data[weather]?.min!!
                    }
                    if (answer[weather]?.max!! < data[weather]?.max!!) {
                        answer[weather]?.max = data[weather]?.max!!
                    }

                }
                else{
                    answer[weather] =
                        data[weather]?.let { data[weather]?.let { it1 -> data[weather]?.let { it2 -> data[weather]?.let { it3 -> WeatherData(it.sum, it1.min, it2.max, it3.count) } } } }!!

                }
            }
        }

        for (weather in answer.toSortedMap()){
            println("${weather.key}=${weather.value.min}/${String.format("%.1f",weather.value.sum / weather.value.count)}" +
                "/${weather.value.max}")
        }


    }
    println("\n\nTime in Seconds : ${executionTime / 1000.0} ")

//    val directoryPath = "answer/"
//    for (directory in "abcdefghijklmnopqrstuvwxyz"){
//        printDetails("$directoryPath/$directory")
//
//    }
//    printDetails(directoryPath+"zothers")


}

fun handleFile(file: File): HashMap<String, WeatherData> {
    val file = BufferedReader(FileReader("measurements.txt"))
    val weatherDataMap: HashMap<String, WeatherData> = HashMap<String, WeatherData>()
    //    val fileContent = file.readLines()
    var line = file.readLine()
    while ( line != null ) {
        val lineData = line.split(';')
        val name = lineData[0]
        val value = lineData[1].toDouble()
        //        var directoryName = name[0].lowercase()
        //        if (directoryName in characters){
        //
        //        }
        //        else{
        //            directoryName = "zothers"
        //        }
        //        val fileName = "answer/$directoryName/$name.txt"
        //        val writingFile = File(fileName)
        //        if (writingFile.exists()){
        //            val fileData = writingFile.readText().split(';')
        //            val sum = fileData[0].toDouble() + value
        //            var min = fileData[1].toDouble()
        //            if (min>value){
        //                min=value
        //            }
        //            var max = fileData[2].toDouble()
        //            if (max<value){
        //                max = value
        //            }
        //            val count = fileData[3].toInt()+1
        //            writingFile.writeText("$sum;$min;$max;$count")
        //
        //        }else{
        //            val writer = FileWriter(fileName)
        //            writer. write("$value;$value;$value;1")
        //            writer. close()
        //        }
        if (weatherDataMap.contains(name)){
            weatherDataMap[name]?.sum = weatherDataMap[name]?.sum?.plus(value)!!
            weatherDataMap[name]?.count = weatherDataMap[name]?.count?.plus(1)!!
            if (weatherDataMap[name]?.min!! > value){
                weatherDataMap[name]?.min = value
            }
            if (weatherDataMap[name]?.max!! < value) {
                weatherDataMap[name]?.max = value
            }

        }
        else{
            weatherDataMap[name] = WeatherData(value, value, value, 1)

        }
        line = file.readLine()
    }
    return weatherDataMap
//    for (weather in weatherDataMap.toSortedMap()){
//        println("${weather.key}=${weather.value.min}/${String.format("%.1f",weather.value.sum / weather.value.count)}" +
//                "/${weather.value.max}")
//    }
}

fun printDetails(directory:String){
    val files = File(directory).listFiles()
    for (file in files){
        val weatherFile = File("$$directory/${file.name}")
        val weatherData = weatherFile.readText().split(';')

        println("${file.name.substring(0, file.name.length-4)}=" +
                "${weatherData[1]}/" +
                String.format("%.1f",weatherData[0].toDouble()/weatherData[3].toInt()) +
                "/${weatherData[2]}")
    }
}

@Throws(IOException::class)
fun splitFile(file: File, sizeOfFileInMB: Int): Int {
    var counter = 1
    val files: MutableList<File> = ArrayList()
    val sizeOfChunk = 1024 * 1024 * sizeOfFileInMB
    val eof = System.lineSeparator()
    BufferedReader(FileReader(file)).use { br ->
        val name = file.name
        var line = br.readLine()
        while (line != null) {
            var (fileName, extension) = name.split('.')
            val newFile = File("measurements/$fileName$counter.txt"
            )
            BufferedOutputStream(FileOutputStream(newFile)).use { out ->
                var fileSize = 0
                while (line != null) {
                    val bytes =
                        (line + eof).toByteArray(Charset.defaultCharset())
                    if (fileSize + bytes.size > sizeOfChunk) break
                    out.write(bytes)
                    fileSize += bytes.size
                    line = br.readLine()
                }
            }
            files.add(newFile)
        }
    }
    return counter
}