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
    path: 'packages',
    renderMode: RenderMode.Prerender
  },
  {
    path: 'packages/:id',
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
  {
    path: 'blog',
    renderMode: RenderMode.Prerender
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];