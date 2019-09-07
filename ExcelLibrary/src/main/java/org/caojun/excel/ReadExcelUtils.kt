package org.caojun.excel

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.caojun.utils.FileUtils

object ReadExcelUtils {

    private val hmWorkbook = HashMap<String, XSSFWorkbook>()
    private val hmSheet = HashMap<String, XSSFSheet>()
    private val hmRow = HashMap<String, Row>()
    private val hmCell = HashMap<String, Cell>()

    init {
        init()
    }

    private fun init() {
        hmWorkbook.clear()
        hmSheet.clear()
        hmRow.clear()
        hmCell.clear()
    }

    /**
     * 第一层：Workbook
     */
    fun getWorkbook(filePath: String): XSSFWorkbook? {
        val key: String = filePath
        if (hmWorkbook.containsKey(key)) {
            return hmWorkbook[key]
        }
        val inputStream = FileUtils.getInputStream(filePath) ?: return null
        val workbook = XSSFWorkbook(inputStream)
        hmWorkbook[key] = workbook
        return workbook
    }

    fun getNumberOfSheets(filePath: String): Int {
        val workbook = getWorkbook(filePath) ?: return 0
        return workbook.numberOfSheets
    }

    /**
     * 第二层：Sheet
     */
    fun getSheet(filePath: String, index: Int): XSSFSheet? {
        val key = "$filePath-$index"
        if (hmSheet.containsKey(key)) {
            return hmSheet[key]
        }
        if (index < 0) {
            return null
        }
        val workbook = getWorkbook(filePath) ?: return null
        if (index >= workbook.numberOfSheets) {
            return null
        }
        val sheet = workbook.getSheetAt(index)
        hmSheet[key] = sheet
        return sheet
    }

    fun getNumberOfRows(filePath: String, indexSheet: Int): Int {
        val sheet = getSheet(filePath, indexSheet) ?: return 0
        return sheet.physicalNumberOfRows
    }

    /**
     * 第三层：Row
     */
    fun getRow(filePath: String, indexSheet: Int, index: Int): Row? {
        val key = "$filePath-$indexSheet-$index"
        if (hmRow.containsKey(key)) {
            return hmRow[key]
        }
        if (index < 0) {
            return null
        }
        val sheet = getSheet(filePath, indexSheet) ?: return null
        if (index >= sheet.physicalNumberOfRows) {
            return null
        }
        val row = sheet.getRow(index)
        hmRow[key] = row
        return row
    }

    fun getNumberOfCells(filePath: String, indexSheet: Int, indexRow: Int): Int {
        val row = getRow(filePath, indexSheet, indexRow) ?: return 0
        return row.physicalNumberOfCells
    }

    /**
     * 第四层：Cell
     */
    fun getCell(filePath: String, indexSheet: Int, indexRow: Int, index: Int): Cell? {
        val key = "$filePath-$indexSheet-$indexRow-$index"
        if (hmCell.containsKey(key)) {
            return hmCell[key]
        }
        if (index < 0) {
            return null
        }
        val row = getRow(filePath, indexSheet, indexRow) ?: return null
        if (index >= row.physicalNumberOfCells) {
            return null
        }
        val cell = row.getCell(index)
        hmCell[key] = cell
        return cell
    }
}