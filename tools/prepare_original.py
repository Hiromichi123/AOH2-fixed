"""Prepare ignored local build inputs from the user's matching original EXE."""
from pathlib import Path
import argparse,hashlib,zipfile
EXPECTED='5300d13579f51e8e8dc69f0a24cf6bafe22297754cfebb4701f7a92e6b5ebe66'
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('exe',type=Path,help='Path to the unmodified supported AoC2.exe')
args=parser.parse_args();source=args.exe.resolve();data=source.read_bytes()
if hashlib.sha256(data).hexdigest()!=EXPECTED:raise SystemExit('Unsupported original EXE. Do not use a patched executable as input.')
root=Path(__file__).resolve().parents[1];output=root/'original';output.mkdir(exist_ok=True)
backup=output/'AoC2-before-launcher-patch.exe'
if backup.exists() and backup.read_bytes()!=data:raise SystemExit('Existing build input differs; refusing to overwrite it.')
if source!=backup.resolve():backup.write_bytes(data)
with zipfile.ZipFile(source) as original,zipfile.ZipFile(output/'AoC2-exe.jar','w',zipfile.ZIP_DEFLATED) as target:
 for info in original.infolist():target.writestr(info,original.read(info.filename))
print('Prepared ignored original/ build inputs. No installed game files were changed.')
