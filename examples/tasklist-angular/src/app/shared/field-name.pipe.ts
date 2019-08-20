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
    const lastDot = key.lastIndexOf('.');
    if (lastDot !== -1) {
      key = key.substring(lastDot + 1);
    }
    return key.split(split).join(separator).toLowerCase();
  }
}
