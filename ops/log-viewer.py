from http.server import ThreadingHTTPServer, BaseHTTPRequestHandler
from pathlib import Path
import base64,hmac,json,secrets,os
ROOT=Path(__file__).resolve().parent
AUTH=ROOT/'.log-viewer-auth'
if not AUTH.exists():
 AUTH.write_text('admin:'+secrets.token_urlsafe(18));AUTH.chmod(0o600)
expected='Basic '+base64.b64encode(AUTH.read_text().strip().encode()).decode()
HTML='''<!doctype html><html lang="es"><meta charset="utf-8"><meta name="viewport" content="width=device-width"><title>Spring · Logs</title><style>body{margin:0;background:#111827;color:#dbe3ef;font:14px system-ui}header{padding:24px 30px;background:#182235;border-bottom:1px solid #2d3a50}h1{font-size:23px;margin:0 0 8px}p{color:#93a6bf;margin:0}nav{padding:18px 30px;display:flex;gap:12px;flex-wrap:wrap}input,select,button{padding:10px 13px;border:1px solid #38465e;background:#1d2b41;border-radius:6px;color:#dbe3ef}input{min-width:280px}button{cursor:pointer}pre{margin:0 30px 30px;white-space:pre-wrap;overflow-wrap:anywhere;font:12px/1.7 Consolas,monospace}.error{color:#ff8e9c}.warn{color:#eec477}#status{padding:0 30px 18px;color:#91a5c0}</style><header><h1>Spring · Visor de logs</h1><p>Autopartes Backend · últimas líneas del archivo · solo lectura</p></header><nav><input id="q" placeholder="Buscar texto en los logs" aria-label="Buscar"><select id="level" aria-label="Nivel"><option value="">Todos los niveles</option><option>ERROR</option><option>WARN</option><option>INFO</option><option>DEBUG</option></select><button id="pause">Pausar</button><button id="refresh">Actualizar</button></nav><div id="status" role="status">Cargando…</div><pre id="logs"></pre><script>let paused=false,lines=[];const q=document.querySelector('#q'),level=document.querySelector('#level'),log=document.querySelector('#logs'),status=document.querySelector('#status');function draw(){log.replaceChildren();let count=0;for(const line of lines){if(q.value&&!line.toLowerCase().includes(q.value.toLowerCase()))continue;if(level.value&&!line.includes(level.value))continue;const el=document.createElement('div');el.textContent=line;if(line.includes('ERROR')||line.includes('Exception'))el.className='error';else if(line.includes('WARN'))el.className='warn';log.append(el);count++;}status.textContent=count+' líneas visibles · '+new Date().toLocaleTimeString()+(paused?' · Pausado':' · Actualización cada 3 segundos');}async function refresh(){try{const r=await fetch('/logs',{cache:'no-store'});if(!r.ok)throw Error();lines=await r.json();draw();}catch{status.textContent='No se pudieron leer los logs. Intenta actualizar.'}}q.oninput=draw;level.onchange=draw;document.querySelector('#pause').onclick=function(){paused=!paused;this.textContent=paused?'Reanudar':'Pausar';if(!paused)refresh();else draw()};document.querySelector('#refresh').onclick=refresh;setInterval(()=>{if(!paused)refresh()},3000);refresh();</script></html>'''
class Handler(BaseHTTPRequestHandler):
 def do_GET(self):
  if not hmac.compare_digest(self.headers.get('Authorization',''),expected):
   self.send_response(401);self.send_header('WWW-Authenticate','Basic realm="Spring Logs"');self.end_headers();return
  if self.path not in ('/','/logs'):
   self.send_error(404);return
  try:
   if self.path=='/logs':
    with open('/home/oswq/proyectos_servidor/backend/spring.log','rb') as f:
     f.seek(0,2);size=f.tell();f.seek(max(0,size-256000));data=f.read().decode('utf-8',errors='replace').splitlines()[-1000:]
    body=json.dumps(data).encode();mime='application/json'
   else:body=HTML.encode();mime='text/html; charset=utf-8'
   self.send_response(200);self.send_header('Content-Type',mime);self.send_header('Cache-Control','no-store');self.send_header('X-Content-Type-Options','nosniff');self.send_header('X-Frame-Options','DENY');self.end_headers();self.wfile.write(body)
  except OSError:self.send_error(503)
 def log_message(self,*args):pass
ThreadingHTTPServer(('100.86.116.110',9092),Handler).serve_forever()