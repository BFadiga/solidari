import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AppComponent } from './app.component';
import { routes } from './app.routes';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [provideRouter(routes), provideHttpClient()]
    }).compileComponents();
  });

  it('deve criar o app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('deve exibir a marca e o menu de navegação', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const tela: HTMLElement = fixture.nativeElement;

    expect(tela.querySelector('.marca')?.textContent).toContain('Solidari');
    const links = Array.from(tela.querySelectorAll('nav a')).map(a => a.textContent?.trim());
    expect(links).toEqual(['Início', 'Carteira', 'Parceiros']);
  });

  it('deve mostrar o botão Entrar quando ninguém está logado', () => {
    localStorage.clear();
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.btn-entrar')).toBeTruthy();
  });
});
