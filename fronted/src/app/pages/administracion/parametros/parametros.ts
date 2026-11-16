import { texto, telefonoValido, coordenadaValida, correoValido, mapaValido } from './parametros.validators';
import { Component, DestroyRef, computed, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DomSanitizer } from '@angular/platform-browser';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { toast } from 'ngx-sonner';
import { ParametrosService, ParametrosTienda } from '../../../_services/parametros.service';
@Component({selector:'app-parametros-page',standalone:true,imports:[ReactiveFormsModule],templateUrl:'./parametros.html',styleUrl:'./parametros.css'})
export class ParametrosPage {
 private service=inject(ParametrosService);private fb=inject(FormBuilder);private destroy=inject(DestroyRef);private sanitizer=inject(DomSanitizer);
 cargando=true;guardando=false;error='';version=0;cargado=false;
 form=this.fb.nonNullable.group({
  nombre:['',texto(2,150)],local:['',texto(2,200)],telefono:['',telefonoValido],
  whatsapp:['',[Validators.required,Validators.pattern(/^[1-9][0-9]{6,14}$/)]],
  direccion:['',texto(5,300)],correoContacto:['',[Validators.email,Validators.maxLength(254),correoValido]],horario:['',texto(3,300,true)],
  latitud:[0,[coordenadaValida,Validators.min(-90),Validators.max(90)]],longitud:[0,[coordenadaValida,Validators.min(-180),Validators.max(180)]],
  mapsUrl:['',[Validators.maxLength(2048),mapaValido]],
 });
 private cambios=toSignal(this.form.valueChanges,{initialValue:this.form.getRawValue()});
 readonly mapa=computed(()=>{const d=this.cambios();if(typeof d.latitud!=='number' || typeof d.longitud!=='number' || !Number.isFinite(d.latitud) || !Number.isFinite(d.longitud) || Math.abs(d.latitud)>90 || Math.abs(d.longitud)>180)return null;return this.sanitizer.bypassSecurityTrustResourceUrl(`https://www.google.com/maps?q=${encodeURIComponent(d.latitud+','+d.longitud)}&z=17&output=embed`);});
 readonly whatsappPreview=computed(()=>{const n=this.cambios().whatsapp || '';return /^[1-9][0-9]{6,14}$/.test(n)?`https://wa.me/${n}`:null;});
 constructor(){this.cargar();}
 private mostrar(p:ParametrosTienda){this.version=p.version;this.cargado=true;this.form.reset({nombre:p.nombre,local:p.local,telefono:p.telefono,whatsapp:p.whatsapp,direccion:p.direccion,correoContacto:p.correoContacto || '',horario:p.horario || '',latitud:p.latitud,longitud:p.longitud,mapsUrl:p.mapsUrl || ''});}
 cargar(){if(this.guardando)return;this.cargando=true;this.error='';this.service.cargar().pipe(finalize(()=>this.cargando=false),takeUntilDestroyed(this.destroy)).subscribe({next:p=>this.mostrar(p),error:()=>this.error='No pudimos cargar los parámetros. Vuelve a intentarlo.'});}
 normalizar(campo:keyof typeof this.form.controls){const control=this.form.controls[campo];if(typeof control.value==='string'){(control as import('@angular/forms').FormControl<string>).setValue(control.value.trim());}control.markAsTouched();}
 guardar(){if(this.guardando || this.cargando || !this.cargado)return;for(const campo of ['nombre','local','telefono','whatsapp','direccion','correoContacto','horario','mapsUrl'] as const)this.normalizar(campo);this.form.markAllAsTouched();if(this.form.invalid){this.error='Revisa los campos marcados antes de guardar.';return;}const d=this.form.getRawValue();for(const k of ['nombre','local','telefono','whatsapp','direccion','correoContacto','horario','mapsUrl'] as const)d[k]=d[k].trim();this.guardando=true;this.error='';this.form.disable();this.service.guardar({...d,version:this.version}).pipe(finalize(()=>{this.guardando=false;}),takeUntilDestroyed(this.destroy)).subscribe({next:p=>{this.form.enable();this.mostrar(p);toast.success('Parámetros guardados. El sitio ya usa los datos nuevos.');},error:e=>{this.form.enable();const detalles=e.status===400?e.error?.data:null;let campos=false;if(detalles && typeof detalles==='object'){for(const [campo,mensaje] of Object.entries(detalles)){if(campo in this.form.controls && typeof mensaje==='string'){const control=this.form.controls[campo as keyof typeof this.form.controls];control.setErrors({servidor:mensaje});control.markAsTouched();campos=true;}}}this.error=campos?'Revisa los campos marcados.':e.error?.message || e.message || 'No se pudieron guardar los parámetros.';toast.error(this.error);}});}
 mensaje(campo:keyof typeof this.form.controls):string{
  const e=this.form.controls[campo].errors;if(!e)return '';
  if(e['servidor'])return e['servidor'];if(e['required'])return 'Este campo es obligatorio.';
  if(e['minlength'])return `Ingresa al menos ${e['minlength'].requiredLength} caracteres.`;
  if(e['maxlength'])return `Usa como máximo ${e['maxlength'].requiredLength} caracteres.`;
  if(e['caracteres'])return 'No uses saltos de línea ni caracteres de control.';
  if(campo==='telefono')return 'Ingresa un teléfono con 7 a 15 dígitos. Solo se admite + al inicio, espacios, paréntesis o guiones.';
  if(campo==='whatsapp')return 'Ingresa de 7 a 15 dígitos con código de país, sin + ni espacios.';
  if(campo==='correoContacto')return 'Ingresa un correo completo, por ejemplo contacto@tienda.com.';
  if(campo==='mapsUrl')return 'Usa un enlace HTTPS de Google Maps, no la página principal de Google.';
  if(campo==='latitud' || campo==='longitud')return campo==='latitud'?'Usa un número entre -90 y 90, con hasta 7 decimales.':'Usa un número entre -180 y 180, con hasta 7 decimales.';
  return 'Revisa el valor ingresado.';
 }
 invalido(campo:keyof typeof this.form.controls){const c=this.form.controls[campo];return c.touched && c.invalid;}
}
