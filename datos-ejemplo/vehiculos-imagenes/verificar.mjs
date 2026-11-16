import fs from 'node:fs/promises';
import { createHash } from 'node:crypto';
const root='backend/datos-ejemplo/vehiculos-imagenes';
const files=(await fs.readdir(root)).filter(n=>/^registros-\d+\.json$/.test(n)).sort();
const manifest=JSON.parse(await fs.readFile(root+'/'+files.at(-1),'utf8'));
const base='http://localhost:4200';
let models;
for(let n=0;n<12;n++){try{const r=await fetch(base+'/api/modelos-vehiculos',{signal:AbortSignal.timeout(2500)});if(!r.ok)throw Error(r.status);models=(await r.json()).data;break}catch(e){if(n===11)throw e;await new Promise(resolve=>setTimeout(resolve,2000));}}
if(models.length!==manifest.length)throw Error('El número de modelos no coincide con el manifiesto.');
const remaining=[...manifest];let checked=0;const errors=[];
await Promise.all(Array.from({length:6},async()=>{
 while(remaining.length){const item=remaining.shift();try{
  const model=models.find(m=>m.id===item.id);if(!model||model.imagenUrl!==item.url)throw Error('El registro no tiene la URL esperada');
  const r=await fetch(base+model.imagenUrl,{signal:AbortSignal.timeout(30000)});if(!r.ok)throw Error('HTTP '+r.status);
  if(!r.headers.get('content-type')?.startsWith('image/'))throw Error('Respuesta no es imagen');
  const hash=createHash('sha256');for await(const chunk of r.body)hash.update(chunk);
  const local=createHash('sha256').update(await fs.readFile(root+'/'+item.archivo)).digest('hex');
  if(hash.digest('hex')!==local)throw Error('Contenido diferente al original');
  checked++; console.log(`Verificada ${checked}/${manifest.length}: ${item.marca} ${item.modelo}`);
 }catch(e){errors.push({modelo:item.marca+' '+item.modelo,error:String(e)});console.log('ERROR: '+item.modelo+' '+e);}}
}));
const productRes=await fetch(base+'/api/productos');const products=(await productRes.json()).data;
const report={modelos:models.length,imagenesVerificadas:checked,productosConservados:products.length,errores:errors};
await fs.writeFile(root+'/verificacion.json',JSON.stringify(report,null,2));console.log(JSON.stringify(report));if(errors.length)process.exitCode=1;
