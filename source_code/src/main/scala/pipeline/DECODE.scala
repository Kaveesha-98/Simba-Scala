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

    val itype_imm = Seq((53, 32), (30, 20))
    val stype_imm = Seq((53, 32), (30, 25), (11, 7))
    val btype_imm = Seq((52, 32), (7, 7), (30, 25), (11, 8), (1, 0))
    val utype_imm = Seq((32, 32), (31, 12), (12, 0))
    val jtype_imm = Seq((44, 32), (19, 12), (20, 20), (30, 25), (24, 21), (1, 0))
    val ntype_imm = Seq((64, 0))
    val rtype_imm = Seq((64, 0))

    val IMM_EXT = Seq(rtype_imm, itype_imm, stype_imm, btype_imm, utype_imm, jtype_imm, ntype_imm)
}

class DECODE_UNIT extends Module{
	val io = IO(new Bundle{
		val INSTRUCTION = Input(UInt(32.W))
		
		val out = Output(UInt(64.W))
	})
	
	val type_w = pipelineParams.INS_TYPE_ROM.foldRight(pipelineParams.ntype.U(3.W))((ins_entry, otherwise) => {
		ins_entry match {
			case (opcode, ins_type ) => Mux(opcode.U === io.INSTRUCTION(6, 0), ins_type.U(3.W), otherwise)
		}
	})
	
    val IMM_EXT = VecInit.tabulate(pipelineParams.IMM_EXT.length)(i => {
        Cat(pipelineParams.IMM_EXT(i).map(imm_map => {
            imm_map match {
                case (x, 32) => Fill(x, io.INSTRUCTION(31))
                case (x, 0)  => 0.U(x.W)
                case (x, y)  => io.INSTRUCTION(x, y)
            }
        }))
    })
    
    val imm_out = IMM_EXT(type_w)

	io.out := imm_out
}

object DECODE_UNIT extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new DECODE_UNIT())
}
