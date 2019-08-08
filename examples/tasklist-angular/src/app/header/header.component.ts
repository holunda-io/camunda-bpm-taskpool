import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'tasks-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  isNavbarCollapsed = false;

  constructor() { }

  ngOnInit() {
  }

}
