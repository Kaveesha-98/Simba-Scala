package pipeline

import chisel3._
import chisel3.util._
import chisel3.Driver

import common.paramFunctions
import common.pipelineParams

class controlIO extends Bundle{
    val A_BUS_SEL = UInt(1.W)
    val B_BUS_SEL = UInt(1.W)
    val ALU_CNT = UInt(4.W)
    val CSR_CNT = UInt(4.W)
    val TYPE = UInt(2.W)
    val undefined = UInt(1.W)
    }

object contorlUnit {  
    def ctrlUnitSignals(A_BUS_SEL: String, B_BUS_SEL: String, ALU_CNT: String, CSR_CNT: String, TYPE: String, undefined: String) = {
        val controls = Wire(new controlIO)
        controls.A_BUS_SEL := A_BUS_SEL.U
        controls.B_BUS_SEL := B_BUS_SEL.U
        controls.ALU_CNT := ALU_CNT.U
        controls.CSR_CNT := CSR_CNT.U
        controls.TYPE := TYPE.U
        controls.undefined := undefined.U
        controls
    }
    
}