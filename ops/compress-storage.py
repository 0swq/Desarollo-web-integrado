from pathlib import Path
import os,subprocess,json,urllib.request,io,uuid
from PIL import Image,ImageOps
root=Path('/home/oswq/proyectos_servidor/backend');cfg={}
for line in (root/'.env').read_text().splitlines():
 if '=' in line and not line.lstrip().startswith('#'):
  k,v=line.split('=',1);cfg[k.strip()]=v.strip()
env=os.environ.copy();env['PGPASSWORD']=cfg['DB_PASSWORD']
def sql(query):
 return subprocess.check_output(['psql','-v','ON_ERROR_STOP=1','-h','aws-1-ca-central-1.pooler.supabase.com','-U','postgres.ruvuaponemmepdwnqfce','-d','postgres','-tA','-c',query],env=env,text=True).strip()
rows=json.loads(sql("SELECT coalesce(json_agg(t),'[]') FROM (SELECT name,metadata->>'size' AS size FROM storage.objects WHERE bucket_id='autopartes-imagenes' AND (metadata->>'size')::bigint>250000) t"))
backup=root/'image-backup';backup.mkdir(exist_ok=True);manifest=[]
headers={'apikey':cfg['SUPABASE_STORAGE_KEY'],'Authorization':'Bearer '+cfg['SUPABASE_STORAGE_KEY']}
base=cfg['SUPABASE_URL'].rstrip('/')+'/storage/v1';bucket=cfg.get('SUPABASE_STORAGE_BUCKET','autopartes-imagenes')
for row in rows:
 name=row['name'];req=urllib.request.Request(base+'/object/authenticated/'+bucket+'/'+name,headers=headers)
 data=urllib.request.urlopen(req,timeout=40).read();dest=backup/name;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(data)
 im=ImageOps.exif_transpose(Image.open(io.BytesIO(data)));im.thumbnail((1200,1200),Image.Resampling.LANCZOS)
 if im.mode not in ('RGB','RGBA'):im=im.convert('RGBA' if 'transparency' in im.info else 'RGB')
 out=io.BytesIO();im.save(out,format='WEBP',quality=82,method=6);optimized=out.getvalue()
 if len(optimized)>=len(data):continue
 new=name.split('/')[0]+'/'+str(uuid.uuid4())+'.webp'
 req=urllib.request.Request(base+'/object/'+bucket+'/'+new,data=optimized,headers={**headers,'Content-Type':'image/webp','x-upsert':'false'},method='POST');urllib.request.urlopen(req,timeout=40).read()
 oldurl='/api/storage/imagenes/'+name;newurl='/api/storage/imagenes/'+new
 def quote(v):return "'"+v.replace("'","''")+"'"
 query='BEGIN;'
 for table,column in [('producto','imagen_url'),('modelo_vehiculo','imagen_url'),('marca_vehiculo','imagen_url'),('usuario','foto_perfil_url')]:
  query+=f'UPDATE {table} SET {column}={quote(newurl)} WHERE {column}={quote(oldurl)};'
 query+='COMMIT;';sql(query)
 manifest.append({'original':name,'optimized':new,'before':len(data),'after':len(optimized)})
 (backup/'manifest.json').write_text(json.dumps(manifest,indent=2))
 print(name,len(data),'->',len(optimized),flush=True)
print('TOTAL',sum(x['before'] for x in manifest),'->',sum(x['after'] for x in manifest),flush=True)