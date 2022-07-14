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
}