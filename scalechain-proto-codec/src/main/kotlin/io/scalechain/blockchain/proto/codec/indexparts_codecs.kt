package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.primitive.{VarList, FixedByteArray, VarInt}
import scodec.Codec
import scodec.codecs._

object RecordLocatorCodec : MessagePartCodec<RecordLocator> {
  val codec : Codec<RecordLocator> {
    ("offset" | int64L) ::
    ("size"   | int32L)
  }.as<RecordLocator>
}

object FileRecordLocatorCodec : MessagePartCodec<FileRecordLocator>{
  val codec : Codec<FileRecordLocator> {
    ("fileIndex" | int32L ) ::
    ("recordLocator" | RecordLocatorCodec.codec)
  }.as<FileRecordLocator>
}

object BlockFileInfoCodec : MessagePartCodec<BlockFileInfo>{
  val codec : Codec<BlockFileInfo> {
    ("blockCount" | int32L) ::
    ("fileSize" | int64L) ::
    ("fistBlockHeight" | int64L) ::
    ("lastBlockHeight" | int64L) ::
    ("firstBlockTimestamp" | int64L) ::
    ("lastBlockTimestamp" | int64L)
  }.as<BlockFileInfo>
}

object BlockInfoCodec : MessagePartCodec<BlockInfo>{
  val codec : Codec<BlockInfo> {
    ("height" | int64L) ::
    ("chainWork" | int64L) ::
    ("nextBlockHash" | optional(bool(8), HashCodec.codec)) ::
    ("transactionCount" | int32L) ::
    ("status" | int32L) ::
    ("blockHeader" | BlockHeaderCodec.codec) ::
    ("blockLocatorOption" | optional(bool(8), FileRecordLocatorCodec.codec) )
  }.as<BlockInfo>
}

object FileNumberCodec : MessagePartCodec<FileNumber> {
  val codec : Codec<FileNumber> {
    ("file_number" | int32L)
  }.as<FileNumber>
}

/** Writes only one byte, to test the case where a record file has a remaining space.
  */
object OneByteCodec : MessagePartCodec<OneByte>{
  val codec : Codec<OneByte> {
    ("value" | byte)
  }.as<OneByte>
}

object LongValueCodec : MessagePartCodec<LongValue>{
  val codec : Codec<LongValue> {
    ("value" | int64)
  }.as<LongValue>
}

/** The codec for TransactionCount.
  *
  */
object TransactionCountCodec : MessagePartCodec<TransactionCount> {
  val codec : Codec<TransactionCount> {
    ("transactionCount" | VarInt.countCodec )
  }.as<TransactionCount>
}

object BlockHeightCodec : MessagePartCodec<BlockHeight> {
  val codec : Codec<BlockHeight> {
    ("blockHeight" | int64)
  }.as<BlockHeight>
}

object TransactionDescriptorCodec : MessagePartCodec<TransactionDescriptor> {
  val codec : Codec<TransactionDescriptor> {
    ("transactionLocator" | FileRecordLocatorCodec.codec) ::
    ("blockHeight"        | int64) ::
    ("outputsSpentBy"     | VarList.varList( optional(bool(8), InPointCodec.codec) ))
  }.as<TransactionDescriptor>
}

object OrphanBlockDescriptorCodec : MessagePartCodec<OrphanBlockDescriptor> {
  val codec : Codec<OrphanBlockDescriptor> {
    ("block" | BlockCodec.codec)
  }.as<OrphanBlockDescriptor>
}

object OrphanTransactionDescriptorCodec : MessagePartCodec<OrphanTransactionDescriptor> {
  val codec : Codec<OrphanTransactionDescriptor> {
    ("transaction" | TransactionCodec.codec )
  }.as<OrphanTransactionDescriptor>
}

object TransactionPoolEntryCodec : MessagePartCodec<TransactionPoolEntry> {
  val codec : Codec<TransactionPoolEntry> {
    ("transaction" | TransactionCodec.codec ) ::
    ("outputsSpentBy" | VarList.varList( optional(bool(8), InPointCodec.codec) )) ::
    ("createdAt" | int64)
  }.as<TransactionPoolEntry>
}