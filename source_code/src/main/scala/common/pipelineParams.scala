package common

import chisel3._
import chisel3.util._
import chisel3.Driver

object pipelineParams {

	//instruction opcodes 
    val lui      = "b0110111"
    val auipc    = "b0010111"
    val jump     = "b1101111"
    val jumpr    = "b1100111"
    val cjump    = "b1100011"
    val load     = "b0000011"
    val store    = "b0100011"
    val iops     = "b0010011"
    val rops     = "b0110011"
    val system   = "b1110011"  
    val fence    = "b0001111"
    val amos     = "b0101111"
    val iops32   = "b0011011"
    val rops32   = "b0111011"
    
    // INS_TYPE
    val rtype       =  "b000" 
    val itype       =  "b001"  
    val stype       =  "b010"  
    val btype       =  "b011"  
    val utype       =  "b100"  
    val jtype       =  "b101"  
    val ntype       =  "b110" 

    //type of write to be done
    val  idle  = "b00"
    val  ld    = "b01"
    val  alu   = "b10"

    val a_bus_rs2_sel = "b1"
    val a_bus_imm_sel = "b0"
    val b_bus_rs1_sel = "b1"
    val b_bus_pc_sel  = "b0" 

    val alu_add     = "b0000"
    val alu_sub     = "b0001"
    val alu_sll     = "b0010"
    val alu_sltu    = "b0011"
    val alu_xor     = "b0100"
    val alu_srl     = "b0101"
    val alu_sra     = "b0110"
    val alu_or      = "b0111"
    val alu_and     = "b1000"
    val alu_a       = "b1001"
    val alu_b       = "b1010"
    val alu_slt     = "b1011"
    val alu_b4      = "b1100"
    val alu_idle    = "b1101"
    val alu_csr     = "b1110"
    val alu_mstd    = "b1111"

    //csr OPS
    val sys_idle    = "b0000" 
    
    val sys_ecall   = "b0001"
    val sys_ebreak  = "b0010"
    val sys_uret    = "b0011"
    val sys_sret    = "b0100"
    val sys_mret    = "b0101"
    val sys_wfi     = "b0110"
    
    val sys_csrrw   = "b1001"
    val sys_csrrs   = "b1010"
    val sys_csrrc   = "b1011"
    val sys_csrrwi  = "b1101"
    val sys_csrrsi  = "b1110"
    val sys_csrrci  = "b1111"
}