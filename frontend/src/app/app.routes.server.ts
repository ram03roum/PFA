import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: '',
    renderMode: RenderMode.Prerender
  },
  {
    path: 'login',
    renderMode: RenderMode.Client
  },
  {
    path: 'destinations',
    renderMode: RenderMode.Prerender
  },
  {
    path: 'destinations/:id',
    renderMode: RenderMode.Prerender
  },
  {
    path: 'about',
    renderMode: RenderMode.Prerender
  },
  {
    path: 'contact',
    renderMode: RenderMode.Prerender
  },
  { path: 'favoris', renderMode: RenderMode.Server },
  {
    path: '**',
    renderMode: RenderMode.Server
  }
];