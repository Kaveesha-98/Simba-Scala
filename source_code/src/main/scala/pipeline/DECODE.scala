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
    
    val INS_TYPE_ROM = Seq((lui, utype), (auipc, utype), (jump, jtype), (jumpr, itype), (cjump, btype), (load, itype), (store, stype), (iops, itype), (iops32, itype), (rops, rtype), (rops32, rtype), (system, itype), (fence, ntype), (amos, rtype))
	//default :   TYPE=ntype;
}

class DECODE_UNIT extends Module{
	val io = IO(new Bundle{
		val INSTRUCTION = Input(UInt(32.W))
		
		val out = Output(UInt(3.W))
	})
	
	val type_w = pipelineParams.INS_TYPE_ROM.foldRight(pipelineParams.ntype.U(3.W))((ins_entry, otherwise) => {
		ins_entry match {
			case (opcode, ins_type ) => Mux(opcode.U === io.INSTRUCTION(6, 0), ins_type.U(3.W), otherwise)
		}
	})
	
    
	io.out := type_w
}

object DECODE_UNIT extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new DECODE_UNIT())
}
