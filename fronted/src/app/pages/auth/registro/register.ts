import { Component, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../../_services/auth.service';
import { toast } from 'ngx-sonner';
@Component({selector:'app-register',standalone:true,imports:[CommonModule,ReactiveFormsModule,RouterLink],templateUrl:'./register.html',styleUrls:['./register.css']})
export class RegisterComponent {
 private authService=inject(AuthService);private router=inject(Router);private route=inject(ActivatedRoute);private destroy=inject(DestroyRef);private fb=inject(FormBuilder);
 enviado=false;cargando=false;errorMensaje='';mostrarPassword=false;
 regForm=this.fb.nonNullable.group({
  nombre:['',[Validators.required,Validators.maxLength(100),Validators.pattern(/.*\S.*/)]],apellido:['',[Validators.required,Validators.maxLength(100),Validators.pattern(/.*\S.*/)]],correo:['',[Validators.required,Validators.email,Validators.maxLength(254)]],
  telefono:['',[Validators.maxLength(20),Validators.pattern(/^[0-9+() -]*$/)]],direccion:['',Validators.maxLength(255)],
  contrasena:['',[Validators.required,Validators.minLength(8),Validators.maxLength(72),Validators.pattern(/.*\S.*/)]],repeatPass:['',Validators.required],
 },{validators:form=>form.get('contrasena')?.value===form.get('repeatPass')?.value?null:{notSame:true}});
 get form(){return this.regForm.controls;}
 get regreso(){return this.route.snapshot.queryParamMap.get('returnUrl');}
 onSubmit(){
  if(this.cargando)return;this.enviado=true;this.regForm.markAllAsTouched();if(this.regForm.invalid)return;
  const {repeatPass,...datos}=this.regForm.getRawValue();
  if(new TextEncoder().encode(datos.contrasena).length>72){this.errorMensaje='La contraseña es demasiado larga. Usa menos caracteres especiales.';return;}
  datos.nombre=datos.nombre.trim();datos.apellido=datos.apellido.trim();datos.correo=datos.correo.trim().toLowerCase();datos.telefono=datos.telefono.trim();datos.direccion=datos.direccion.trim();
  this.errorMensaje='';this.cargando=true;
  this.authService.registrar(datos).pipe(finalize(()=>this.cargando=false),takeUntilDestroyed(this.destroy)).subscribe({
   next:res=>{if(!res.success){this.errorMensaje=res.message || 'No se pudo completar el registro.';toast.error(this.errorMensaje);return;}toast.success('Cuenta creada. Ya puedes iniciar sesión.');void this.router.navigate(['/iniciar-sesion'],{queryParams:{returnUrl:this.regreso}});},
   error:e=>{this.errorMensaje=e.status===0?'No pudimos conectar con el servidor. Inténtalo nuevamente.':e.error?.message || 'No se pudo crear la cuenta. Revisa tus datos.';toast.error(this.errorMensaje);},
  });
 }
 onReset(){if(this.cargando)return;this.enviado=false;this.errorMensaje='';this.regForm.reset();}
}
