import chisel3._
import chisel3.util._
import chisel3.Driver

class DECODE_UNIT extends Module{
	val io = IO(new Bundle{
		val INSTRUCTION = Input(UInt(32.W))
        val WB_DATA     = Input(UInt(64.W))
        val WB_DES      = Input(UInt(5.W))
        val TYPE_MEM3_WB= Input(UInt(2.W))
		
		val out = Output(UInt(64.W))
        val out2 = Output(UInt(64.W))
        val out3 = Output(UInt(64.W))
	})
	
	/* val type_w = RegNext(pipelineParams.INS_TYPE_ROM.foldRight(pipelineParams.ntype.U(3.W))((ins_entry, otherwise) => {
		ins_entry match {
			case (opcode, ins_type ) => Mux(opcode === io.INSTRUCTION(6, 0), ins_type, otherwise)
		}
	})) */
    val type_w = RegNext(pipelineParams.INS_TYPE_ROM(io.INSTRUCTION(6, 0)))
	
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

    val rs1_sel_mux = VecInit.tabulate(8)(i => {
        if (Seq(0, 1, 2, 3).contains(i)) {io.INSTRUCTION(19, 15)}
        else {0.U(5.W)}
    })

    val rs1_sel = rs1_sel_mux(type_w)

    val rs2_sel_mux = VecInit.tabulate(8)(i => {
        if (Seq(0, 2, 3).contains(i)) {io.INSTRUCTION(24, 20)}
        else {0.U(5.W)}
    })

    val rs2_sel = rs2_sel_mux(type_w)

    val registerFile = new Array[chisel3.UInt](32)
    val REG_ARRAY = VecInit.tabulate(32)(i => {
        if (i == 0) { registerFile(i) = 0.U(64.W) }
        else { 
            registerFile(i) = RegInit((if (i == 2) "h10000" else "h0").U(64.W))
            when (io.WB_DES === i.U & (io.TYPE_MEM3_WB =/= pipelineParams.idle.U)){
                registerFile(i) := io.WB_DATA
            }
        }

        registerFile(i)
    })

    val rs1_out = REG_ARRAY(rs1_sel)
    val rs2_out = REG_ARRAY(rs2_sel)

	io.out := imm_out

    io.out2 := rs1_out
    io.out3 := rs2_out

    /* val x = RegInit(0.U(1.W))
    val y = WireInit(0.U(1.W))

    when(x){
        y := 1.U
    } */
}

object DECODE_UNIT extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new DECODE_UNIT())
}
