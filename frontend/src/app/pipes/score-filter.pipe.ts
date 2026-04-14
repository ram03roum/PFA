import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'scoreFilter', standalone: true })
export class ScoreFilterPipe implements PipeTransform {
  transform(users: any[], segment: string): number {
    if (!users) return 0;
    return users.filter(u => u.segment === segment).length;
  }
}