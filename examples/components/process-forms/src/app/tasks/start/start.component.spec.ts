import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {of} from 'rxjs-compat/observable/of';
import {StartComponent} from './start.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {EnvironmentHelperService} from 'app/services/environment.helper.service';
import {RequestService} from 'process/services/request.service';
import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ApprovalRequestDraft} from 'process/models/approval-request-draft';
import {RequestFormComponent} from 'app/components/request-form/request-form.component';

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

  beforeEach(waitForAsync(() => {

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
        FormsModule,
        ReactiveFormsModule
      ],
      declarations: [
        StartComponent,
        StubRequestComponent,
        RequestFormComponent
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

