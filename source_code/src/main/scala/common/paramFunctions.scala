package common

import chisel3._
import chisel3.util._
import chisel3.Driver

import pipelineParams._

abstract class instructionFormats {

    def compareOpFields(machineInstr: UInt): Bool

}

case class rEncode(funct7: String, funct3: String, opcode: String) extends instructionFormats {

    def compareOpFields(machineInstr: UInt) = {
        this.opcode.U === machineInstr(6, 0) &&
        this.funct3.U === machineInstr(14, 12) &&
        this.funct7.U === machineInstr(31, 25)
    }

}

case class iEncode(funct3: String, opcode: String) extends instructionFormats {

    def compareOpFields(machineInstr: UInt) = {
        this.opcode.U === machineInstr(6, 0) &&
        this.funct3.U === machineInstr(14, 12)
    }

}

case class shamt5b(funct6: String, funct3: String, opcode: String) extends instructionFormats {

    def compareOpFields(machineInstr: UInt) = {
        this.opcode.U === machineInstr(6, 0) &&
        this.funct3.U === machineInstr(14, 12) &&
        this.funct6.U === machineInstr(31, 26)
    }

}

case class uEncode(opcode: String) extends instructionFormats {

    def compareOpFields(machineInstr: UInt) = {
        this.opcode.U === machineInstr(6, 0)
    }

}

object paramFunctions {

    /**
     * function used map a hardware wire to a output
     *
     * @param mapEntries Seq[(inputMatchCase, result)]
     * @param default output when none of the inputMatchCase matches
     * @param f Matching function(function that compares inputMatchCase and mapInput)
     * @param mapInput input to hardware implemented map
     * @return TODO(Kaveesha)
     */
    def implementLookUp[A,T<:Data,U<:Data](mapSeq: Seq[A], mapEntries: Map[A , T], default: T)
        ( mapInput: U)( f: (A, U) => Bool): T = {
        val conditionArray = mapSeq.map(mapCondition => f(mapCondition, mapInput) -> mapEntries(mapCondition))

        MuxCase(default, conditionArray)
    }

    def implementRuntimeLookUp[A,T<:Data,U<:Data,W<:Data](inputSeq: Seq[A], default: T, g: (A, W) => T, f: (A, U) => Bool)(
        machineInstruction: W, type_w: U): T = {

        val resultMap = createRuntimeLookUpMap(inputSeq, machineInstruction, g)

        implementLookUp(inputSeq, resultMap, default)(type_w)(f)

    }

    def createRuntimeLookUpMap[A,T<:Data,W<:Data](inputSeq: Seq[A], machineInstruction: W, g: (A, W) => T) = {

        inputSeq.map(inputType =>  (inputType -> g(inputType, machineInstruction))).toMap
    }

    val typeSeq = Seq(rtype, itype, stype, btype, utype, jtype, ntype) 

    val opSeq = Seq(lui, auipc, jump, jumpr, cjump, load, store, iops, iops32, rops, rops32, system, fence, amos)

    //opcode -> type_w
    val instrOpToTypeMap = Map(lui      -> utype.U, 
                               auipc    -> utype.U, 
                               jump     -> jtype.U, 
                               jumpr    -> itype.U, 
                               cjump    -> btype.U, 
                               load     -> itype.U, 
                               store    -> stype.U, 
                               iops     -> itype.U, 
                               iops32   -> itype.U, 
                               rops     -> rtype.U, 
                               rops32   -> rtype.U, 
                               system   -> itype.U, 
                               fence    -> ntype.U, 
                               amos     -> rtype.U)
	//default :   TYPE=ntype;
    def INS_TYPE_ROM(machineInstr: UInt) = 
        implementLookUp(opSeq, instrOpToTypeMap, ntype.U)(machineInstr)((x, y) => x.U === y)

    /*for (x, 32) -> repeat instruction(31) x times
          (x, 0) -> repeat 1'b0 x times
          (x, y) -> instruction(x, y)
    */
    val itype_imm = Seq((53, 32), (30, 20))
    val stype_imm = Seq((53, 32), (30, 25), (11, 7))
    val btype_imm = Seq((52, 32), (7, 7), (30, 25), (11, 8), (1, 0))
    val utype_imm = Seq((32, 32), (31, 12), (12, 0))
    val jtype_imm = Seq((44, 32), (19, 12), (20, 20), (30, 25), (24, 21), (1, 0))
    val ntype_imm = Seq((64, 0))
    val rtype_imm = Seq((64, 0))

    //val IMM_EXT = Seq(rtype_imm, itype_imm, stype_imm, btype_imm, utype_imm, jtype_imm, ntype_imm)

    val immediateEncodingsMap = Map(rtype -> rtype_imm, 
                                    itype -> itype_imm,
                                    stype -> stype_imm, 
                                    btype -> btype_imm,
                                    utype -> utype_imm, 
                                    jtype -> jtype_imm,
                                    ntype -> ntype_imm)

    def immediateCreation(instructionType: String, machineInstruction: UInt) = {
        Cat(immediateEncodingsMap(instructionType).map {
            case (x, 32) => Fill(x, machineInstruction(31))
            case (x, 0) => 0.U(x.W)
            case (x, y) => machineInstruction(x, y)})
    }

    val IMM_EXT = implementRuntimeLookUp(typeSeq, 0.U(64.W), immediateCreation, (x: String, y: UInt) => x.U === y)_

    val rs1ValidTypes = Seq(rtype, itype, stype, btype)
    val rs2ValidTypes = Seq(rtype, stype, btype)

    def regSourceSel(validTypes: Seq[String]) = (instructionType: String, instrRegField: UInt) => {
        if (validTypes.contains(instructionType)) instrRegField else 0.U(5.W) 
    }

    val rs1_sel_mux = implementRuntimeLookUp(typeSeq, 0.U(5.W), regSourceSel(rs1ValidTypes), (x: String, y: UInt) => x.U === y)_

    val rs2_sel_mux = implementRuntimeLookUp(typeSeq, 0.U(5.W), regSourceSel(rs2ValidTypes), (x: String, y: UInt) => x.U === y)_

}