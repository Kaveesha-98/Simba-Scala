import chisel3._
import chisel3.util._
import chisel3.Driver

object pipelineParams {

    def mapInputToOutput[T <: Data](mapEntries: Seq[(T, T)], default: T, f: (T, T) => chisel3.Bool)( mapInput: T): T = {
        val conditionArray = Seq.tabulate(mapEntries.length)(i => {
            mapEntries(i) match {
                case (matchDataCase, matchResult) => f(matchDataCase, mapInput) -> matchResult
            }
        })

        MuxCase(default, conditionArray)
    }

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
    
    val INS_TYPE_ROM = Seq((lui.U, utype.U), (auipc.U, utype.U), (jump.U, jtype.U), (jumpr.U, itype.U), (cjump.U, btype.U), (load.U, itype.U), (store.U, stype.U), (iops.U, itype.U), (iops32.U, itype.U), (rops.U, rtype.U), (rops32.U, rtype.U), (system.U, itype.U), (fence.U, ntype.U), (amos.U, rtype.U))
	//default :   TYPE=ntype;
    val getOpTypeFor = mapInputToOutput(INS_TYPE_ROM, ntype.U, (x: chisel3.UInt, y: chisel3.UInt) => x === y)(_)

    val itype_imm = Seq((53, 32), (30, 20))
    val stype_imm = Seq((53, 32), (30, 25), (11, 7))
    val btype_imm = Seq((52, 32), (7, 7), (30, 25), (11, 8), (1, 0))
    val utype_imm = Seq((32, 32), (31, 12), (12, 0))
    val jtype_imm = Seq((44, 32), (19, 12), (20, 20), (30, 25), (24, 21), (1, 0))
    val ntype_imm = Seq((64, 0))
    val rtype_imm = Seq((64, 0))

    val IMM_EXT = Seq(rtype_imm, itype_imm, stype_imm, btype_imm, utype_imm, jtype_imm, ntype_imm)
}