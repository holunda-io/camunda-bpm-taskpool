import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'fieldName'
})
export class FieldNamePipe implements PipeTransform {

  transform(fieldName: string | string ): String {
    const sentence = this.toSentence(fieldName);
    return sentence.charAt(0).toUpperCase() + sentence.slice(1);
  }

  private toSentence(key) {
    const separator = ' ';
    const split = /(?=[A-Z])/;
    return key.split(split).join(separator).toLowerCase();
  }
}
