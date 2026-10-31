from pathlib import Path
import zipfile,struct,hashlib,json
root=Path.cwd();base=root/'original/AoC2-before-launcher-patch.exe';target=root/'dist/AoC2-fixed.exe';folder=root/'release/AoC2-Bugfix-1.0.0'
folder.mkdir(parents=True,exist_ok=True)
a=base.read_bytes();b=target.read_bytes();ops=[];literal=bytearray()
def emit(kind,offset,length):
 if not length:return
 if ops and ops[-1][0]==kind and ops[-1][1]+ops[-1][2]==offset:ops[-1][2]+=length
 else:ops.append([kind,offset,length])
def data(value):
 off=len(literal);literal.extend(value);emit('d',off,len(value))
def data_start(buf,info):
 off=info.header_offset;assert buf[off:off+4]==b'PK\x03\x04';n,e=struct.unpack_from('<HH',buf,off+26);return off+30+n+e
copied=0
with zipfile.ZipFile(base) as za,zipfile.ZipFile(target) as zb:
 pos=0;index={i.filename:i for i in za.infolist()}
 start=min(i.header_offset for i in zb.infolist());assert a[:start]==b[:start];emit('c',0,start);copied+=start;pos=start
 for item in sorted(zb.infolist(),key=lambda x:x.header_offset):
  begin=data_start(b,item);data(b[pos:begin]);end=begin+item.compress_size;old=index.get(item.filename)
  if old:
   oldbegin=data_start(a,old);chunk=a[oldbegin:oldbegin+old.compress_size]
  else:chunk=None
  if chunk==b[begin:end]:emit('c',oldbegin,len(chunk));copied+=len(chunk)
  else:data(b[begin:end])
  pos=end
 data(b[pos:])
manifest={'format':'aoc2-copy-literal-v1','version':'1.0.0','appid':603850,'source_sha256':hashlib.sha256(a).hexdigest(),'target_sha256':hashlib.sha256(b).hexdigest(),'source_size':len(a),'target_size':len(b),'operations':ops}
with zipfile.ZipFile(folder/'patch-data.zip','w',zipfile.ZIP_DEFLATED) as z:
 z.writestr('manifest.json',json.dumps(manifest,separators=(',',':')))
 z.writestr('literal.bin',literal)
reconstructed=b''.join(a[o:o+n] if kind=='c' else literal[o:o+n] for kind,o,n in ops);assert reconstructed==b
print(json.dumps({'operations':len(ops),'bytes_reused':copied,'literal_bytes':len(literal),'patch_bytes':(folder/'patch-data.zip').stat().st_size,'original_exe':len(a),'rebuilt_verified':True}))
