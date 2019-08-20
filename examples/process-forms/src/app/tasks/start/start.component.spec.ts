import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {of} from 'rxjs-compat/observable/of';
import {StartComponent} from './start.component';
import {FormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {EnvironmentHelperService} from 'app/services/environment.helper.service';
import {RequestService} from 'process/api/request.service';
import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ApprovalRequestDraft} from 'process/model/approvalRequestDraft';

@Component({
  selector: 'app-request',
  template: '',
})
export class StubRequestComponent {
  @Input()
  approvalRequest: ApprovalRequestDraft;

  @Output()
  approvalRequestChange = new EventEmitter<ApprovalRequestDraft>();

  @Output()
  isValid = new EventEmitter<Object>();
}

describe('Component: StartComponent', () => {

  let component: StartComponent;
  let fixture: ComponentFixture<StartComponent>;

  beforeEach(async(() => {

    const requestServiceSpy = jasmine.createSpyObj('RequestService', {
      start: of({}),
    });
    const envSpy = jasmine.createSpyObj('EnvironmentHelperService', {
      env: of({
        applicationName: 'foo',
        tasklistUrl: 'http://bar',
      }),
      none: {
        applicationName: 'foo',
        tasklistUrl: 'http://bar',
      }
    });

    TestBed.configureTestingModule({
      imports: [
        FormsModule
      ],
      declarations: [
        StartComponent,
        StubRequestComponent
      ],
      providers: [
        {
          provide: ActivatedRoute, useValue: {
            snapshot: {
              queryParams: {'userId': 'some-id'}
            }
          },
        },
        {provide: Router, useValue: jasmine.createSpyObj('Router', {'navigate': {}})},
        {provide: RequestService, useValue: requestServiceSpy},
        {provide: EnvironmentHelperService, useValue: envSpy},
        StubRequestComponent
      ],
    }).compileComponents().then(() => {
      // create component and test fixture
      fixture = TestBed.createComponent(StartComponent);

      // get test component from the fixture
      component = fixture.componentInstance;

      // detect changes
      fixture.detectChanges();
    });
  }));

  it('should create', () => {
    expect(component).toBeDefined();
  });

});

