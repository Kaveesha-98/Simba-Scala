package pipeline

import chisel3._
import chisel3.util._
import chisel3.Driver

import common._
import common.paramFunctions
import common.pipelineParams._

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

    val lutCtrlUnit = Seq(
        //RV32I Base instruction set
        (new insCmp(opcode = "b0110111"), ctrlUnitSignals(a_bus_imm_sel, b_bus_pc_sel, alu_a, sys_idle, alu, "b0")), //lui
        (new insCmp(opcode = "b0010111"), ctrlUnitSignals(a_bus_imm_sel, b_bus_pc_sel, alu_a, sys_idle, alu, "b0")), //auipc
        (new insCmp(opcode = "b1101111"), ctrlUnitSignals(a_bus_imm_sel, b_bus_pc_sel, alu_b4, sys_idle, alu, "b0")), //jal
        (new insCmp(opcode = "b1100111"), ctrlUnitSignals(a_bus_imm_sel, b_bus_pc_sel, alu_b4, sys_idle, alu, "b0")), //jalr
        (new insCmp(opcode = "b1100011"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_idle, sys_idle, idle, "b0")), //beq, bne, blt, bge, bltu, bgeu 
        (new insCmp(opcode = "b0000011"), ctrlUnitSignals(a_bus_imm_sel, b_bus_rs1_sel, alu_add, sys_idle, ld, "b0")), // lb, lh, lw, lbu, lhu
        (new insCmp(opcode = "b0110011", funct3 = "b000", funct7 = "b0000000"), ctrlUnitSignals(a_bus_imm_sel, b_bus_pc_sel, alu_add, sys_idle, alu, "b0"))
    )
    
}