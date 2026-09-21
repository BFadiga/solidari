import { Routes } from '@angular/router';
import { AdminComponent } from './pages/admin/admin.component';
import { CarteiraComponent } from './pages/carteira/carteira.component';
import { EntrarComponent } from './pages/entrar/entrar.component';
import { HomeComponent } from './pages/home/home.component';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'entrar', component: EntrarComponent },
  { path: 'carteira', component: CarteiraComponent },
  { path: 'admin', component: AdminComponent },
  { path: '**', redirectTo: 'home' }
];
