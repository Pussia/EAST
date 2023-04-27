package com.soloupis.sample.ocr_keras.utils

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

class CTCLabelConverter(character: String, separatorList: Map<String, String>, dictPathList: Map<String, String>) {

    private val dict: HashMap<String, Int> = HashMap()
    private val character: ArrayList<String> = ArrayList()
    private var separatorList: Map<String, String>?
    private var ignoreIndex: ArrayList<Int> = ArrayList()
    private var listOfLines: List<String>? = null
    private var dictOfLines: Map<String, String>? = null
    private var isDict: Boolean = false

    init {
        val dictCharacter: List<String> = character.toCharArray().map { ch -> ch.toString() }
        for (i in dictCharacter.indices) {
            this.dict[dictCharacter[i]] = i + 1
        }

        this.character.add("[blank]")
        this.character.addAll(dictCharacter)
        this.separatorList = separatorList

        val separatorChar: ArrayList<String> = ArrayList()
        separatorChar.addAll(separatorList.values)

        for (i in (0..separatorChar.size + 1)) {
            this.ignoreIndex.add(i)
        }

        if (separatorList.isEmpty()) {
            val dictList: ArrayList<String> = ArrayList()
            for (path in dictPathList.values) {
                val file = File(path)
                try {
                    BufferedReader(FileReader(file)).use { br ->
                        var line: String?
                        while (br.readLine().also { line = it } != null) {
                            line?.let { dictList.add(it) }
                        }
                    }
                } catch (ignored: IOException) {}
            }
            this.listOfLines = dictList;
        } else {
            val dictList: HashMap<String, String> = HashMap()
            for (path in dictPathList.entries) {
                val file = File(path.value)
                try {
                    BufferedReader(FileReader(file)).use { br ->
                        var line: String?
                        while (br.readLine().also { line = it } != null) {
                            line?.let { dictList[path.key] = line!! }
                        }
                    }
                } catch (ignored: IOException) {}
            }
            this.dictOfLines = dictList;
            this.isDict = true
        }
    }

    fun decodeGreedy(textIndex: IntArray, length: IntArray): List<String> {
        val texts: ArrayList<String> = ArrayList()
        var index: Int = 0

        for (l in length) {
            val t: IntArray = textIndex.copyOfRange(index, index + 1);

            val stringBuilder: StringBuilder = StringBuilder()
            for (i in (0 until l)) {
                if (!(this.ignoreIndex.contains(textIndex[i])) && (!(i > 0 && textIndex[i - 1] == textIndex[i]))) {
                    stringBuilder.append(this.character[textIndex[i]])
                }
            }

            texts.add(stringBuilder.toString())
            index += l
        }

        return texts
    }
}