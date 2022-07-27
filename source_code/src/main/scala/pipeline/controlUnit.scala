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
        (new insCmp(opcode = "b0000011"), ctrlUnitSignals(a_bus_imm_sel, b_bus_rs1_sel, alu_add, sys_idle, ld, "b0")), //lb, lh, lw, lbu, lhu
        (new insCmp(opcode = "b0100011"), ctrlUnitSignals(a_bus_imm_sel, b_bus_rs1_sel, alu_add, sys_idle, idle, "b0")),//sb, sh, sw
        (new insCmp(opcode = "b0010011", funct3 = "b000"), ctrlUnitSignals(a_bus_imm_sel, b_bus_rs1_sel, alu_add, sys_idle, alu, "b0")),//addi
        (new insCmp(opcode = "b0010011", funct3 = "b010"), ctrlUnitSignals(a_bus_imm_sel, b_bus_rs1_sel, alu_slt, sys_idle, alu, "b0")),//slti
        (new insCmp(opcode = "b0010011", funct3 = "b011"), ctrlUnitSignals(a_bus_imm_sel, b_bus_rs1_sel, alu_sltu, sys_idle, alu, "b0")),//sltiu
        (new insCmp(opcode = "b0010011", funct3 = "b100"), ctrlUnitSignals(a_bus_imm_sel, b_bus_rs1_sel, alu_xor, sys_idle, alu, "b0")),//xori
        (new insCmp(opcode = "b0010011", funct3 = "b110"), ctrlUnitSignals(a_bus_imm_sel, b_bus_rs1_sel, alu_or, sys_idle, alu, "b0")),//ori
        (new insCmp(opcode = "b0010011", funct3 = "b111"), ctrlUnitSignals(a_bus_imm_sel, b_bus_rs1_sel, alu_and, sys_idle, alu, "b0")),//andi
        (new insCmp(opcode = "b0010011", funct3 = "b001", funct6 = "b000000"), ctrlUnitSignals(a_bus_imm_sel, b_bus_rs1_sel, alu_sll, sys_idle, alu, "b0")),//slli
        (new insCmp(opcode = "b0010011", funct3 = "b101", funct6 = "b000000"), ctrlUnitSignals(a_bus_imm_sel, b_bus_rs1_sel, alu_srl, sys_idle, alu, "b0")),//srli
        (new insCmp(opcode = "b0010011", funct3 = "b001", funct6 = "b010000"), ctrlUnitSignals(a_bus_imm_sel, b_bus_rs1_sel, alu_sra, sys_idle, alu, "b0")),//srai
        (new insCmp(opcode = "b0110011", funct3 = "b000", funct7 = "b0000000"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_add, sys_idle, alu, "b0")),//add
        (new insCmp(opcode = "b0110011", funct3 = "b000", funct7 = "b0100000"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_sub, sys_idle, alu, "b0")),//sub
        (new insCmp(opcode = "b0110011", funct3 = "b001", funct7 = "b0000000"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_sll, sys_idle, alu, "b0")),//sll
        (new insCmp(opcode = "b0110011", funct3 = "b010", funct7 = "b0000000"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_slt, sys_idle, alu, "b0")),//slt
        (new insCmp(opcode = "b0110011", funct3 = "b011", funct7 = "b0000000"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_sltu, sys_idle, alu, "b0")),//sltu
        (new insCmp(opcode = "b0110011", funct3 = "b100", funct7 = "b0000000"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_xor, sys_idle, alu, "b0")),//xor
        (new insCmp(opcode = "b0110011", funct3 = "b101", funct7 = "b0000000"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_srl, sys_idle, alu, "b0")),//srl
        (new insCmp(opcode = "b0110011", funct3 = "b101", funct7 = "b0100000"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_sra, sys_idle, alu, "b0")),//sra
        (new insCmp(opcode = "b0110011", funct3 = "b110", funct7 = "b0000000"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_or, sys_idle, alu, "b0")),//or
        (new insCmp(opcode = "b0110011", funct3 = "b111", funct7 = "b0000000"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_and, sys_idle, alu, "b0")),//and
        (new insCmp(opcode = "b0110011", funct3 = "b000", funct7 = "b0000001"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_mstd, sys_idle, alu, "b0")),//mul
        (new insCmp(opcode = "b0110011", funct3 = "b001", funct7 = "b0000001"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_mstd, sys_idle, alu, "b0")),//mulh
        (new insCmp(opcode = "b0110011", funct3 = "b010", funct7 = "b0000001"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_mstd, sys_idle, alu, "b0")),//mulhsu
        (new insCmp(opcode = "b0110011", funct3 = "b011", funct7 = "b0000001"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_mstd, sys_idle, alu, "b0")),//mulhu
        (new insCmp(opcode = "b0110011", funct3 = "b100", funct7 = "b0000001"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_mstd, sys_idle, alu, "b0")),//div
        (new insCmp(opcode = "b0110011", funct3 = "b101", funct7 = "b0000001"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_mstd, sys_idle, alu, "b0")),//divu
        (new insCmp(opcode = "b0110011", funct3 = "b110", funct7 = "b0000001"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_mstd, sys_idle, alu, "b0")),//rem
        (new insCmp(opcode = "b0110011", funct3 = "b111", funct7 = "b0000001"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_mstd, sys_idle, alu, "b0")),//remu
        (new insCmp(opcode = "b0011011", funct3 = "b000"), ctrlUnitSignals(a_bus_imm_sel, b_bus_rs1_sel, alu_add, sys_idle, alu, "b0")),//addiw
        (new insCmp(opcode = "b0011011", funct3 = "b001", funct7 = "b0000000"), ctrlUnitSignals(a_bus_imm_sel, b_bus_rs1_sel, alu_sll, sys_idle, alu, "b0")),//slliw
        (new insCmp(opcode = "b0011011", funct3 = "b101", funct7 = "b0000000"), ctrlUnitSignals(a_bus_imm_sel, b_bus_rs1_sel, alu_srl, sys_idle, alu, "b0")),//srliw
        (new insCmp(opcode = "b0011011", funct3 = "b101", funct7 = "b0100000"), ctrlUnitSignals(a_bus_imm_sel, b_bus_rs1_sel, alu_sra, sys_idle, alu, "b0")),//sraiw
        (new insCmp(opcode = "b0111011", funct3 = "b000", funct7 = "b0000000"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_add, sys_idle, alu, "b0")),//addw
        (new insCmp(opcode = "b0111011", funct3 = "b000", funct7 = "b0100000"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_sub, sys_idle, alu, "b0")),//subw
        (new insCmp(opcode = "b0111011", funct3 = "b001", funct7 = "b0000000"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_sll, sys_idle, alu, "b0")),//sllw
        (new insCmp(opcode = "b0111011", funct3 = "b101", funct7 = "b0000000"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_srl, sys_idle, alu, "b0")),//srlw
        (new insCmp(opcode = "b0111011", funct3 = "b101", funct7 = "b0100000"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_sra, sys_idle, alu, "b0")),//sraw
        (new insCmp(opcode = "b0111011", funct3 = "b000", funct7 = "b0000001"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_mstd, sys_idle, alu, "b0")),//mulw
        (new insCmp(opcode = "b0111011", funct3 = "b100", funct7 = "b0000001"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_mstd, sys_idle, alu, "b0")),//divw
        (new insCmp(opcode = "b0111011", funct3 = "b101", funct7 = "b0000001"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_mstd, sys_idle, alu, "b0")),//divuw
        (new insCmp(opcode = "b0111011", funct3 = "b110", funct7 = "b0000001"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_mstd, sys_idle, alu, "b0")),//remw
        (new insCmp(opcode = "b0111011", funct3 = "b111", funct7 = "b0000001"), ctrlUnitSignals(a_bus_rs2_sel, b_bus_rs1_sel, alu_mstd, sys_idle, alu, "b0"))//remuw
        //(new insCmp(opcode = "b1110011", rd = "b00000", funct3 = "b000", rs1 = "b00000", rs2 = "b00000", funct7 = "b0000000"), ctrlUnitSignals()),//ecall
        //(new insCmp(opcode = "b1110011", rd = "b00000", funct3 = "b000", rs1 = "b00000", rs2 = "b00001", funct7 = "b0000000"), ctrlUnitSignals()),//ebreak

    )
    
}