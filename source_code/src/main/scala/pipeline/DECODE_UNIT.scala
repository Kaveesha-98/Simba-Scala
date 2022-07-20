package pipeline

import chisel3._
import chisel3.util._
import chisel3.Driver

import common._
import common.paramFunctions
import common.pipelineParams

class DECODE_UNIT extends Module{
	val io = IO(new Bundle{
		val INSTRUCTION = Input(UInt(32.W))
        val WB_DATA     = Input(UInt(64.W))
        val WB_DES      = Input(UInt(5.W))
        val TYPE_MEM3_WB= Input(UInt(2.W))

        val testInput = Input(UInt(64.W))
		
		val out = Output(UInt(64.W))
        val out2 = Output(UInt(64.W))
        val out3 = Output(UInt(64.W))
	})
	
	/* opcode of the instruction is mapped to number according to paramFunctions.instructionOpcodeToTypeMap */
    val type_w = WireInit(paramFunctions.INS_TYPE_ROM(io.INSTRUCTION(6, 0)))
	
    /* immediate according type_w generated according the opcode */
    val imm_out = paramFunctions.IMM_EXT(io.INSTRUCTION, type_w)

    /* selects which register to read as rs1 */
    val rs1_sel = paramFunctions.rs1_sel_mux(io.INSTRUCTION(19, 15), type_w)
    
    /* selects which register to read as rs2 */
    val rs2_sel = paramFunctions.rs2_sel_mux(io.INSTRUCTION(24, 20), type_w)

    /* register file of the cpu */
    val registerFile = new Array[chisel3.UInt](32)
    val REG_ARRAY = VecInit.tabulate(32)(i => {
        if (i == 0) { registerFile(i) = 0.U(64.W) } //x0 is hardwired to ground
        else { 
            registerFile(i) = RegInit((if (i == 2) "h10000" else "h0").U(64.W))// when reset x2 is set to 32'h10000
            when (io.WB_DES === i.U & (io.TYPE_MEM3_WB =/= pipelineParams.idle.U)){
                registerFile(i) := io.WB_DATA
            }
        }

        registerFile(i)
    })

    val rs1_out = REG_ARRAY(rs1_sel)
    val rs2_out = REG_ARRAY(rs2_sel)

	io.out := imm_out

    io.out2 := rs1_out + 1.U
    //io.out3 := rs2_out

    val testEncoding = rEncode(opcode = "b1111000", funct3 = "b101", funct7 = "b1100110")
    io.out3 := Mux(testEncoding.compareOpFields(io.testInput), 1.U, 0.U)

    /* val x = RegInit(0.U(1.W))
    val y = WireInit(0.U(1.W))

    when(x){
        y := 1.U
    } */
}

object DECODE_UNIT extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new DECODE_UNIT())
}
